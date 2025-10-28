package org.piet.forumbackend.content.reports.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.piet.forumbackend.content.comments.Comment;
import org.piet.forumbackend.content.comments.CommentsService;
import org.piet.forumbackend.content.comments.exceptions.CommentNotFoundException;
import org.piet.forumbackend.content.reports.dtos.CreateCommentReportDto;
import org.piet.forumbackend.content.reports.services.CommentReportService;
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
@RequestMapping("${forum.api.prefix}/reports/comments")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Comment reports", description = "Endpoints for handling comment reports")
public class CommentReportController {

    private final CommentReportService commentReportService;
    private final CommentsService commentsService;
    private final UserService userService;

    @PostMapping("/report")
    ResponseEntity<?> createCommentReport(@RequestBody CreateCommentReportDto dto, Authentication auth) throws CommentNotFoundException {
        Comment comment = commentsService.getContentById(dto.getCommentId());
        User user = null;
        try {
            user = userService.getUserFromAuth(auth);
            log.info("User with ID: {} reported a comment!", user.getId());
        } catch (UserNotLoggedInException ex){
            log.info("Anonymous user reported a comment!");
        }
        finally {
            commentReportService.createReport(dto.getReason(), comment, user);
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


}
