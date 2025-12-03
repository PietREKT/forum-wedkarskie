package org.piet.forumbackend.content.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.content.dtos.ContentDtoMapper;
import org.piet.forumbackend.content.dtos.requests.content.CreateContentDto;
import org.piet.forumbackend.content.dtos.requests.content.EditContentDto;
import org.piet.forumbackend.content.dtos.responses.content.ContentDto;
import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.content.entities.enums.ContentType;
import org.piet.forumbackend.content.entities.enums.VoteType;
import org.piet.forumbackend.content.services.ContentService;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserServiceImpl;
import org.piet.forumbackend.users.groups.entities.UserGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("${forum.api.prefix}/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Endpoint for comments management")
public class CommentController {
    private final UserServiceImpl userService;
    private final ContentService contentService;


    @Value("${forum.constants.comments.pageSize}")
    Integer PAGE_SIZE;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContentDto> createComment(
            @ModelAttribute CreateContentDto dto) throws UserNotLoggedInException, NotFoundException, IOException, BadRequestException, UnauthorizedAccessException {
        Content parent = contentService.getContentByIdOrNull(dto.getParentId());
        UserGroup group = parent != null ? parent.getGroup() : null;
        Content comment = contentService.createContent(userService.getCurrentUser(), dto.getContent(), ContentType.COMMENT, parent, group , dto.getPhotos());

        return ResponseEntity.ok(ContentDtoMapper.toContentDto(comment));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PageDto<ContentDto>> getPostComments(
            @PathVariable Long postId,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            Authentication auth
    ) throws NotFoundException {
        User user = userService.getUserFromAuthOrNull(auth);
        Pageable p = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
        var comments = contentService.getContentByParent(postId, user, p);
        return ResponseEntity.ok(comments);
    }

    @PatchMapping
    public ResponseEntity<ContentDto> editComment(@RequestBody EditContentDto dto) throws UserNotLoggedInException, NotFoundException, UnauthorizedAccessException, BadRequestException, IOException {
        Content comment = contentService.editContent(userService.getCurrentUser(), dto.getId(), dto.getContent(), dto.getAttachedPhotos(), dto.getNewPhotos());
        return ResponseEntity.ok(ContentDtoMapper.toContentDto(comment));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) throws UserNotLoggedInException, UnauthorizedAccessException, NotFoundException {
        contentService.deleteContent(commentId, userService.getCurrentUser());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{commentId}/upvote")
    public ResponseEntity<?> upvote(@PathVariable Long commentId) throws UserNotLoggedInException, NotFoundException {
        Content c = contentService.getContentById(commentId);
        contentService.vote(c, userService.getCurrentUser(), VoteType.UPVOTE);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{commentId}/downvote")
    public ResponseEntity<?> downvote(@PathVariable Long commentId) throws UserNotLoggedInException, NotFoundException {
        Content c = contentService.getContentById(commentId);
        contentService.vote(c, userService.getCurrentUser(), VoteType.DOWNVOTE);
        return ResponseEntity.ok().build();
    }
}
