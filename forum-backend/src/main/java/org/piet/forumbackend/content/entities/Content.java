package org.piet.forumbackend.content.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.content.entities.enums.ContentType;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.groups.entities.UserGroup;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
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
    @JoinColumn(name = "parent_id")
    Content parent;

    @ManyToOne
    @JoinTable(
            name = "groups_posts",
            joinColumns = @JoinColumn(name = "content_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id")
    )
    UserGroup group;

    @OneToMany(
            mappedBy = "parent",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Content> children;

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

    @OneToMany(
            mappedBy = "content",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    List<ContentVote> votes = new ArrayList<>();

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();
        editHistory = new HashMap<>();
        editHistory.put(now, getContent());
        setCreatedAt(now);

        if (parent != null){
            this.group = parent.getGroup();
        }
    }

    public Integer getRating() {
        return votes
                .stream()
                .mapToInt(v -> v.getVote().getValue())
                .sum();
    }

    public String toLogString() {
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

    public void addAttachmentUrl(String attachmentUrl) {
        if (this.attachedPhotos == null) {
            this.attachedPhotos = new ArrayList<>();
        }
        this.attachedPhotos.add(attachmentUrl);
    }

    public void removeAttachmentUrl(String attachmentUrl) {
        if (this.attachedPhotos == null || !this.attachedPhotos.contains(attachmentUrl)) return;
        this.attachedPhotos.remove(attachmentUrl);
    }

    public void addChild(Content child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(Content child) {
        children.remove(child);
        child.setParent(null);
    }
}
