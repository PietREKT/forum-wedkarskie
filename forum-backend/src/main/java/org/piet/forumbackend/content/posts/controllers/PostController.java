package org.piet.forumbackend.content.posts.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.piet.forumbackend.content.comments.CommentsService;
import org.piet.forumbackend.content.posts.dtos.PostDto;
import org.piet.forumbackend.content.posts.dtos.PostDtoMapper;
import org.piet.forumbackend.content.posts.dtos.UpdatePostDto;
import org.piet.forumbackend.content.posts.entities.Post;
import org.piet.forumbackend.content.posts.services.PostService;
import org.piet.forumbackend.exceptions.NotFoundException;
import org.piet.forumbackend.users.UserService;
import org.piet.forumbackend.users.entities.User;
import org.piet.forumbackend.users.exceptions.UserNotLoggedInException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("${forum.api.prefix}/posts")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Posts", description = "Endpoints for posts management.")
public class PostController {

    private final UserService userService;
    private final PostService postService;
    private final CommentsService commentsService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostDto> createPost(
            Authentication auth,
            @RequestPart("data") String content,
            @RequestPart(name = "photos", required = false) List<MultipartFile> photos) throws UserNotLoggedInException, IOException {
        User user = userService.getUserFromAuth(auth);
        Post post = postService.createPost(user, content, photos);
        log.info("User: {} added new post: {}", user, post);
        return ResponseEntity.ok(PostDtoMapper.toPostDto(post));
    }

    @PatchMapping("/edit")
    public ResponseEntity<PostDto> updatePost(Authentication auth, @RequestBody UpdatePostDto dto) throws UserNotLoggedInException, NotFoundException {
        User u = userService.getUserFromAuth(auth);
        Post updated = postService.editContent(u, dto.getId(), dto.getContent());
        log.info("User {} changed post's content with id {} to {}", u, updated.getId(), updated.getContent());
        return ResponseEntity.ok(PostDtoMapper.toPostDto(updated));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(Authentication auth, @PathVariable() Long postId) throws UserNotLoggedInException, NotFoundException {
        User u = userService.getUserFromAuth(auth);
        postService.deleteContent(postId, u);
        log.info("User {} deleted post with id: {}", u, postId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{postId}/upvote")
    public ResponseEntity<?> upvotePost(@PathVariable Long postId, Authentication auth) throws NotFoundException, UserNotLoggedInException {
        User u = userService.getUserFromAuth(auth);
        postService.upvote(postId);
        log.info("User: {} upvoted post with id: {}", u, postId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{postId}/downvote")
    public ResponseEntity<?> downvotePost(@PathVariable Long postId, Authentication auth) throws NotFoundException, UserNotLoggedInException {
        User u = userService.getUserFromAuth(auth);
        postService.downvote(postId);
        log.info("User: {} downvoted post with id: {}", u, postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPostDetails(@PathVariable Long postId) throws NotFoundException {
        Post p = postService.getContentById(postId);
        return ResponseEntity.ok(PostDtoMapper.toPostDto(p));
    }
}
