package org.piet.forumbackend.users.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.content.reports.dtos.ContentReportDto;
import org.piet.forumbackend.content.reports.dtos.HotReportedContentDto;
import org.piet.forumbackend.content.reports.entities.enums.TimeUnitInput;
import org.piet.forumbackend.content.reports.services.ContentReportService;
import org.piet.forumbackend.content.services.ContentService;
import org.piet.forumbackend.exceptions.NotFoundException;
import org.piet.forumbackend.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.fish.FishService;
import org.piet.forumbackend.fish.dtos.CreateFishDto;
import org.piet.forumbackend.fish.dtos.FishDto;
import org.piet.forumbackend.fish.dtos.FishDtoMapper;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.pagination.PaginationDto;
import org.piet.forumbackend.users.UserService;
import org.piet.forumbackend.users.entities.User;
import org.piet.forumbackend.users.exceptions.UserNotLoggedInException;
import org.springdoc.core.annotations.ParameterObject;
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
    private final UserService userService;
    private final FishService fishService;
    private final ContentReportService contentReportService;
    private final ContentService contentService;

    @GetMapping("/reports/summary")
    ResponseEntity<List<HotReportedContentDto>> getReportsSummary(
            @ParameterObject PaginationDto paginationDto,
            @RequestParam(name = "amount", required = false, defaultValue = "1") Long amount,
            @RequestParam(name = "unit", required = false, defaultValue = "WEEKS") TimeUnitInput unit
            ) {
        var dtos = contentReportService.getRecentlyReportedContent(amount, unit.map(), paginationDto.getPage(), paginationDto.getSize());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/reports/{contentId}")
    ResponseEntity<ContentReportDto> getContentReports(@PathVariable Long contentId) throws NotFoundException {
        Content content = contentService.getContentById(contentId);
        var dto = contentReportService.getReportSummary(content);
        return ResponseEntity.ok(dto);
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
