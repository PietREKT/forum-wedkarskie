package org.piet.forumbackend.content.core.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.piet.forumbackend.content.core.dtos.requests.tutorials.CreateTutorialDto;
import org.piet.forumbackend.content.core.dtos.responses.tutorials.ListTutorialDto;
import org.piet.forumbackend.content.core.dtos.responses.tutorials.TutorialDto;
import org.piet.forumbackend.content.core.entities.Content;
import org.piet.forumbackend.content.core.entities.enums.VoteType;
import org.piet.forumbackend.content.core.services.ContentService;
import org.piet.forumbackend.content.core.services.TutorialService;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fish.entities.enums.FishingMethod;
import org.piet.forumbackend.fish.services.FishService;
import org.piet.forumbackend.fishing_spots.core.exceptions.FishNotFoundException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserService;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("${forum.api.prefix}/tutorials")
@RequiredArgsConstructor
@Tag(name = "Content - tutorials", description = "Endpoints for tutorials.")
public class TutorialController {
    private final UserService userService;
    private final TutorialService tutorialService;
    private final FishService fishService;
    private final ContentService contentService;

    @PostMapping("/create")
    public ResponseEntity<TutorialDto> createTutorial(@Valid @ModelAttribute CreateTutorialDto dto) throws UserNotLoggedInException, UnauthorizedAccessException, IOException {
        User currentUser = userService.getCurrentUser();
        TutorialDto tut = tutorialService.createTutorial(currentUser, dto.getTitle(), dto.getContent(), dto.getPhotos());

        return ResponseEntity.ok(tut);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TutorialDto> getById(@PathVariable Long id) throws UserNotLoggedInException, NotFoundException {
        User currentUser = userService.getCurrentUser();
        TutorialDto tut = tutorialService.getDtoById(id, currentUser);

        return ResponseEntity.ok(tut);
    }

    @GetMapping
    public ResponseEntity<PageDto<ListTutorialDto>> getTutorialsBulk(PaginationDto pagination){
        var page = tutorialService.getTutorialsVerifiedAsDtos(
                pagination.toPageable(Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        return ResponseEntity.ok(PageDto.of(page));
    }

    @GetMapping("/method")
    public ResponseEntity<PageDto<TutorialDto>> getByMethod(@RequestParam FishingMethod method, PaginationDto pagination) {
        return ResponseEntity.ok(
                PageDto.of(
                        tutorialService.getTutorialsByMethod(method, pagination)
                )
        );
    }
    @GetMapping(path = "/fish", params = {"fishId", "!fishIds"})
    public ResponseEntity<PageDto<TutorialDto>> getByFish(@RequestParam Long fishId, PaginationDto pagination) throws FishNotFoundException {
        Fish fish = fishService.getFishById(fishId);
        return ResponseEntity.ok(
                PageDto.of(
                        tutorialService.getTutorialsByFish(fish, pagination)
                )
        );
    }

    @GetMapping(path = "/fish", params = {"fishIds", "!fishId"})
    public ResponseEntity<PageDto<TutorialDto>> getByFishMultiple(@RequestParam List<Long> fishIds, PaginationDto pagination){
        var dto = tutorialService.getTutorialsByMultipleFish(fishIds, pagination);

        return ResponseEntity.ok(PageDto.of(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) throws UserNotLoggedInException {
        User currentUser = userService.getCurrentUser();
        tutorialService.deleteTutorial(id, currentUser);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{tutorialId}/downvote")
    public ResponseEntity<?> downvoteTutorial(@PathVariable Long tutorialId, Authentication auth) throws NotFoundException, UserNotLoggedInException {
        User u = userService.getUserFromAuth(auth);
        Content tutorial = contentService.getContentById(tutorialId);
        contentService.vote(tutorial, u, VoteType.DOWNVOTE);

        log.info("User: {} downvoted tutorial with id: {}", u, tutorialId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{tutorialId}/upvote")
    public ResponseEntity<?> upvoteTutorial(@PathVariable Long tutorialId, Authentication auth) throws NotFoundException, UserNotLoggedInException {
        User u = userService.getUserFromAuth(auth);
        Content tutorial = contentService.getContentById(tutorialId);
        contentService.vote(tutorial, u, VoteType.UPVOTE);

        log.info("User: {} upvoted tutorial with id: {}", u, tutorialId);
        return ResponseEntity.ok().build();
    }
}
