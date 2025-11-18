package org.piet.forumbackend.content.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.content.entities.enums.ContentType;
import org.piet.forumbackend.users.entities.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(targetEntity = User.class)
    User author;

    String content;

    Instant createdAt;

    @Enumerated(EnumType.STRING)
    ContentType contentType;

    @ManyToOne(targetEntity = Content.class)
    Content parent;

    @ElementCollection
    @CollectionTable(name = "content_photos", joinColumns = @JoinColumn(name = "content_id"))
    @Column(name = "photo_url")
    List<String> attachedPhotos;

    @ElementCollection
    @CollectionTable(
            name = "content_edit_history",
            joinColumns = @JoinColumn(name = "content_id")
    )
    @MapKeyColumn(name = "edited_at")
    @Column(name = "edited_content", columnDefinition = "TEXT")
    Map<Instant, String> editHistory;

    @OneToMany(mappedBy = "content")
    List<ContentVote> votes = new ArrayList<>();

    @PrePersist
    private void onCreate(){
        Instant now = Instant.now();
        editHistory = new HashMap<>();
        editHistory.put(now, getContent());
        setCreatedAt(now);
    }

    public Integer getRating(){
        return votes
                .stream()
                .mapToInt(v -> v.getVote().getValue())
                .sum();
    }

    public String toLogString(){
        return "{ " +
                "ID: " +
                id +
                ", createdAt: " +
                createdAt +
                ", author: " +
                author.getUsername() +
                ", attachments: "
                + (attachedPhotos != null ? attachedPhotos.size() : 0) +
                " }";
    }

    public void addAttachmentUrl(String attachmentUrl){
        if (this.attachedPhotos == null){
            this.attachedPhotos = new ArrayList<>();
        }
        this.attachedPhotos.add(attachmentUrl);
    }

    public void removeAttachmentUrl(String attachmentUrl){
        if (this.attachedPhotos == null || !this.attachedPhotos.contains(attachmentUrl)) return;
        this.attachedPhotos.remove(attachmentUrl);
    }
}
