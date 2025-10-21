package org.piet.forumbackend.posts.services;

import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.posts.entities.Comment;
import org.piet.forumbackend.posts.entities.Post;
import org.piet.forumbackend.posts.repostitories.CommentRepository;
import org.piet.forumbackend.users.entities.User;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentsService {
    private final CommentRepository commentRepository;

    public List<Comment> getCommentsByPostId(Long postId, Pageable pageable){
        return commentRepository.findByPost_Id(postId, pageable);
    }

    public Comment createComment(Post post, String content, User author){
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setContent(content);
        comment.setAuthor(author);
        return commentRepository.save(comment);
    }


}
