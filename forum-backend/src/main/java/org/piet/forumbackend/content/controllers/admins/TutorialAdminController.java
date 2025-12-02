package org.piet.forumbackend.content.controllers.admins;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.content.dtos.requests.tutorials.RejectTutorialDto;
import org.piet.forumbackend.content.dtos.responses.tutorials.TutorialDto;
import org.piet.forumbackend.content.services.TutorialService;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${forum.api.prefix}/admin/tutorials")
@RequiredArgsConstructor
public class TutorialAdminController {

    private final UserService userService;
    private final TutorialService tutorialService;

    @GetMapping("/unverified")
    public ResponseEntity<PageDto<TutorialDto>> getUnverified(PaginationDto pagination) {
        Page<TutorialDto> dtos = tutorialService.getTutorialsUnverified(pagination);

        return ResponseEntity.ok(PageDto.createDto(dtos));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<?> acceptTutorial(@PathVariable Long id) throws UserNotLoggedInException, AccessDeniedException, NotFoundException, BadRequestException {
        tutorialService.acceptTutorial(id, userService.getCurrentUser());

        return ResponseEntity.ok().build();
    }
    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectTutorial(@PathVariable Long id, @Valid @RequestBody RejectTutorialDto dto) throws UserNotLoggedInException, AccessDeniedException, NotFoundException, BadRequestException {
        tutorialService.rejectTutorial(id, dto.getRejectReason(), userService.getCurrentUser());

        return ResponseEntity.ok().build();
    }
}
