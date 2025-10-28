package org.piet.forumbackend.content.reports.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.content.comments.Comment;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class CommentReport extends ContentReport{
    @ManyToOne(targetEntity = Comment.class)
    private Comment reportedComment;

    @PrePersist
    private void onCreated(){
        super.setCreatedAt();
    }
}
