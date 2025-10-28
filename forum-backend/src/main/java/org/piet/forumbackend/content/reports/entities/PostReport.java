package org.piet.forumbackend.content.reports.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.content.posts.entities.Post;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class PostReport extends ContentReport {
    @ManyToOne(targetEntity = Post.class)
    Post reportedPost;

    @PrePersist
    private void onCreated(){
        super.setCreatedAt();
    }
}
