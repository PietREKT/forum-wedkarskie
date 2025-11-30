package org.piet.forumbackend.content.reports.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.content.reports.dtos.CreateContentReportDto;
import org.piet.forumbackend.content.reports.services.ContentReportService;
import org.piet.forumbackend.content.services.ContentService;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.services.UserServiceImpl;
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
public class ContentReportController {

    private final UserServiceImpl userService;
    private final ContentReportService contentReportService;
    private final ContentService contentService;

    @PostMapping("/report")
    ResponseEntity<?> createReport(@RequestBody CreateContentReportDto dto, Authentication auth) throws NotFoundException {
        Content content = contentService.getContentById(dto.getContentId());
        User user = userService.getUserFromAuthOrNull(auth);
        if (user != null){
            log.info("User with ID: {} reported a post!", user.getId());
        } else {
            log.info("Anonymous user reported a post!");
        }
        contentReportService.create(content, user, dto.getReason());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
