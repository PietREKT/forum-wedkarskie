package org.piet.forumbackend.users.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.content.reports.dtos.CommentReportSummaryDto;
import org.piet.forumbackend.content.reports.services.CommentReportService;
import org.piet.forumbackend.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.fish.FishService;
import org.piet.forumbackend.fish.dtos.CreateFishDto;
import org.piet.forumbackend.fish.dtos.FishDto;
import org.piet.forumbackend.fish.dtos.FishDtoMapper;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.users.UserService;
import org.piet.forumbackend.users.entities.User;
import org.piet.forumbackend.users.exceptions.UserNotLoggedInException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("${forum.api.prefix}/admin")
@Tag(name = "Admin", description = "Endpoints for site management.")
@RequiredArgsConstructor
public class AdminController {
    private final CommentReportService commentReportService;
    private final UserService userService;
    private final FishService fishService;

    @GetMapping("/reports/summary")
    ResponseEntity<List<CommentReportSummaryDto>> getCommentSummary(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "count", required = false, defaultValue = "30") int itemsPerPage
    ) {
        Pageable pageable = PageRequest.of(page, itemsPerPage, Sort.by("createdAt").descending());
        List<CommentReportSummaryDto> dtos = commentReportService.getAggregatedReports(pageable);

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/fish/create")
    public ResponseEntity<FishDto> createFish(@ModelAttribute CreateFishDto fishDto, Authentication auth) throws UserNotLoggedInException, IOException {
        User user = userService.getUserFromAuth(auth);
        Fish fish = fishService.createFish(
                fishDto.getName(),
                fishDto.getDescription(),
                fishDto.getAvgLengthCm(),
                fishDto.getAvgMassKg(),
                fishDto.isPredatory(),
                fishDto.getPhoto(),
                fishDto.getMethods(),
                fishDto.getWaterType(), user);
        return ResponseEntity.ok(FishDtoMapper.toFishDto(fish));
    }

    @DeleteMapping("/fish/delete")
    public ResponseEntity<?> deleteFish(@RequestParam String name, Authentication auth) throws UserNotLoggedInException, UnauthorizedAccessException {
        User u = userService.getUserFromAuth(auth);
        fishService.deleteFishByName(name, u);
        return ResponseEntity.ok().build();
    }
}
