package org.piet.forumbackend.content.core.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.piet.forumbackend.content.core.dtos.ContentDtoMapper;
import org.piet.forumbackend.content.core.dtos.requests.content.CreateContentDto;
import org.piet.forumbackend.content.core.dtos.requests.content.EditContentDto;
import org.piet.forumbackend.content.core.dtos.responses.content.ContentDto;
import org.piet.forumbackend.content.core.entities.Content;
import org.piet.forumbackend.content.core.entities.enums.ContentType;
import org.piet.forumbackend.content.core.entities.enums.VoteType;
import org.piet.forumbackend.content.core.services.ContentService;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserService;
import org.piet.forumbackend.users.groups.entities.UserGroup;
import org.piet.forumbackend.users.groups.services.UserGroupService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("${forum.api.prefix}/posts")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Posts", description = "Endpoints for posts management.")
public class PostController {

    private final UserService userService;
    private final ContentService contentService;
    private final UserGroupService userGroupService;


    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContentDto> createPost(
            Authentication auth,
            @ModelAttribute CreateContentDto dto) throws UserNotLoggedInException, IOException, UnauthorizedAccessException, BadRequestException, NotFoundException {

        User user = userService.getUserFromAuth(auth);
        UserGroup userGroup = dto.getGroupId() != null ?
                userGroupService.getById(dto.getGroupId())
                : null;
        Content post = contentService.createContent(user, dto.getContent(), ContentType.POST, null, userGroup, dto.getPhotos());

        log.info("User with id: {} added new post: {}", user.getId(), post.toLogString());
        return ResponseEntity.ok(ContentDtoMapper.toContentDto(post));
    }

    @PatchMapping("/edit")
    public ResponseEntity<ContentDto> updatePost(Authentication auth, @ModelAttribute EditContentDto dto) throws UserNotLoggedInException, NotFoundException, UnauthorizedAccessException, BadRequestException, IOException {
        User u = userService.getUserFromAuth(auth);
        Content updated = contentService.editContent(u, dto.getId(), dto.getContent(), dto.getAttachedPhotos(), dto.getNewPhotos());

        log.info("User {} changed post's content with id {} to {}", u, updated.getId(), updated.getContent());
        return ResponseEntity.ok(ContentDtoMapper.toContentDto(updated));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(Authentication auth, @PathVariable() Long postId) throws UserNotLoggedInException, NotFoundException, UnauthorizedAccessException {
        User u = userService.getUserFromAuth(auth);
        contentService.deleteContent(postId, u);
        log.info("User {} deleted post with id: {}", u, postId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{postId}/upvote")
    public ResponseEntity<?> upvotePost(@PathVariable Long postId, Authentication auth) throws NotFoundException, UserNotLoggedInException {
        User u = userService.getUserFromAuth(auth);
        Content post = contentService.getContentById(postId);
        contentService.vote(post, u, VoteType.UPVOTE);

        log.info("User: {} upvoted post with id: {}", u, postId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{postId}/downvote")
    public ResponseEntity<?> downvotePost(@PathVariable Long postId, Authentication auth) throws NotFoundException, UserNotLoggedInException {
        User u = userService.getUserFromAuth(auth);
        Content post = contentService.getContentById(postId);
        contentService.vote(post, u, VoteType.DOWNVOTE);

        log.info("User: {} downvoted post with id: {}", u, postId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recent")
    public ResponseEntity<PageDto<ContentDto>> getRecentPostsWithUserVote(@ParameterObject PaginationDto paginationDto) throws UserNotLoggedInException {
        var page = contentService.getRecentPosts(paginationDto, userService.getCurrentUserOrNull());

        return ResponseEntity.ok(page);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<PageDto<ContentDto>> getByAuthor(@PathVariable UUID userId , PaginationDto pagination) throws NotFoundException {
        User user = userService.getUserById(userId);
        var posts = contentService.getUserPosts(user, pagination);

        return ResponseEntity.ok(PageDto.of(posts));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentDto> getById(@PathVariable Long id){
        return ResponseEntity.ok(
                contentService.getContentDtoById(id)
        );
    }
}
