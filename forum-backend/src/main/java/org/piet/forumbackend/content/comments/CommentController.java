package org.piet.forumbackend.content.comments;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.content.comments.dtos.CommentDto;
import org.piet.forumbackend.content.comments.dtos.CommentDtoMapper;
import org.piet.forumbackend.content.comments.dtos.EditCommentDto;
import org.piet.forumbackend.content.posts.entities.Post;
import org.piet.forumbackend.content.posts.services.PostService;
import org.piet.forumbackend.exceptions.BadRequestException;
import org.piet.forumbackend.exceptions.NotFoundException;
import org.piet.forumbackend.users.UserService;
import org.piet.forumbackend.users.entities.User;
import org.piet.forumbackend.users.exceptions.UserNotLoggedInException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("${forum.api.prefix}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Endpoint for comments management")
public class CommentController {
    private final CommentsService commentsService;
    private final UserService userService;
    private final PostService postService;

    @Value("${forum.constants.comments.pageSize}")
    Integer PAGE_SIZE;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommentDto> createComment(
            @RequestParam("content") String content,
            @RequestParam(value = "postId") Long postId,
            @RequestParam(value = "commentId", required = false) Long commentId,
            @RequestPart(name = "attachment", required = false) MultipartFile attachment, Authentication auth) throws UserNotLoggedInException, NotFoundException, IOException, BadRequestException {
        User author = userService.getUserFromAuth(auth);
        Post post = postService.getContentById(postId);
        Comment parent = commentsService.getContentByIdOrNull(commentId);
        Comment comment = commentsService.createComment(post, parent, content, author, attachment);

        return ResponseEntity.ok(CommentDtoMapper.toCommentDto(comment));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentDto>> getPostComments(
            @PathVariable Long postId,
            @RequestParam(required = false, defaultValue = "0") Integer page
    ){
        Pageable p = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
        var comments = commentsService.getCommentsByPostId(postId, p);
        return ResponseEntity.ok(comments.stream().map(CommentDtoMapper::toCommentDto).toList());
    }

    @PatchMapping
    public ResponseEntity<CommentDto> editComment(@RequestBody EditCommentDto dto, Authentication auth) throws UserNotLoggedInException, NotFoundException {
        User u = userService.getUserFromAuth(auth);
        Comment comment = commentsService.editContent(u, dto.getId(), dto.getContent());
        return ResponseEntity.ok(CommentDtoMapper.toCommentDto(comment));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, Authentication auth) throws UserNotLoggedInException, NotFoundException {
        User u = userService.getUserFromAuth(auth);
        commentsService.deleteContent(commentId, u);
        return ResponseEntity.ok().build();
    }
}
