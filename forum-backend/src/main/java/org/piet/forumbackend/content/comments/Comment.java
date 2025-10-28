package org.piet.forumbackend.content.comments;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.content.ContentBase;
import org.piet.forumbackend.content.posts.entities.Post;

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

    @PrePersist
    private void onCreate(){
        super.initEditHistory();
    }
}
