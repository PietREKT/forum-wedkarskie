package org.piet.forumbackend.content.comments;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.content.ContentBase;
import org.piet.forumbackend.content.posts.entities.Post;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Comment extends ContentBase {
    @ManyToOne(targetEntity = Post.class)
    Post post;

    @ManyToOne(targetEntity = Comment.class)
    Comment parent;

    String attachmentUrl;

    @ElementCollection
    @CollectionTable(
            name = "comment_edit_history",
            joinColumns = @JoinColumn(name = "comment_id")
    )
    @MapKeyColumn(name = "edited_at")
    @Column(name = "edited_content", columnDefinition = "TEXT")
    Map<Instant, String> editHistory;

    @PrePersist
    private void onCreate(){
        Instant now = Instant.now();
        editHistory = new HashMap<>();
        editHistory.put(now, super.getContent());
        super.setCreatedAt(now);
    }

    @Override
    public String toLogString() {
        String superString = super.toLogString();
        return superString
                .substring(0, superString.length() - 2) +
                ", photoAttached: " + !attachmentUrl.isBlank() +
                " }";
    }
}
