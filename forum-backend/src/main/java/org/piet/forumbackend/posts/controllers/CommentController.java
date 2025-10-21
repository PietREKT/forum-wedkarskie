package org.piet.forumbackend.posts.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.posts.dtos.CommentDto;
import org.piet.forumbackend.posts.dtos.CreateCommentDto;
import org.piet.forumbackend.posts.dtos.PostDtoMapper;
import org.piet.forumbackend.posts.entities.Comment;
import org.piet.forumbackend.posts.entities.Post;
import org.piet.forumbackend.posts.exceptions.PostNotFoundException;
import org.piet.forumbackend.posts.services.CommentsService;
import org.piet.forumbackend.posts.services.PostService;
import org.piet.forumbackend.users.UserNotLoggedInException;
import org.piet.forumbackend.users.UserService;
import org.piet.forumbackend.users.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Endpoint for comments management")
public class CommentController {
    private final CommentsService commentsService;
    private final UserService userService;
    private final PostService postService;

    @Value("${forum.constants.comments.pageSize}")
    Integer PAGE_SIZE;

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@RequestBody CreateCommentDto dto, Authentication auth) throws UserNotLoggedInException, PostNotFoundException {
        User author = userService.getUserFromAuth(auth);
        Post p = postService.getPostById(dto.getPost().getId());
        Comment comment = commentsService.createComment(p, dto.getContent(), author);

        return ResponseEntity.ok(PostDtoMapper.toCommentDto(comment));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentDto>> getPostComments(
            @PathVariable Long postId,
            @RequestParam(required = false, defaultValue = "0") Integer page
    ){
        Pageable p = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
        var comments = commentsService.getCommentsByPostId(postId, p);
        return ResponseEntity.ok(comments.stream().map(PostDtoMapper::toCommentDto).toList());
    }
}
