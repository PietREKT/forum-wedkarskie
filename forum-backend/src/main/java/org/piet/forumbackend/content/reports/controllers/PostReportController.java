package org.piet.forumbackend.content.reports.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.piet.forumbackend.content.posts.entities.Post;
import org.piet.forumbackend.content.posts.exceptions.PostNotFoundException;
import org.piet.forumbackend.content.posts.services.PostService;
import org.piet.forumbackend.content.reports.dtos.CreatePostReportDto;
import org.piet.forumbackend.content.reports.services.PostReportService;
import org.piet.forumbackend.users.UserService;
import org.piet.forumbackend.users.entities.User;
import org.piet.forumbackend.users.exceptions.UserNotLoggedInException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${forum.api.prefix}/reports/posts")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Post reports", description = "Endpoints for handling post reports")
public class PostReportController {

    private final UserService userService;
    private final PostReportService postReportService;
    private final PostService postService;

    @PostMapping("/report")
    ResponseEntity<?> createCommentReport(@RequestBody CreatePostReportDto dto, Authentication auth) throws PostNotFoundException {
        Post post = postService.getContentById(dto.getPostId());
        User user = null;
        try {
            user = userService.getUserFromAuth(auth);
            log.info("User with ID: {} reported a post!", user.getId());
        } catch (UserNotLoggedInException ex){
            log.info("Anonymous user reported a post!");
        }
        finally {
            postReportService.createReport(dto.getReason(), post, user);
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
