package org.piet.forumbackend.posts.services;

import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.posts.entities.Comment;
import org.piet.forumbackend.posts.repostitories.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentsService {
    private final CommentRepository commentRepository;

    public List<Comment> getCommentsByPostId(Long postId){
        return commentRepository.findByPost_Id(postId);
    }
}
