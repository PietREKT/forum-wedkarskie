package org.piet.forumbackend.events.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.events.dtos.EventDtoMapper;
import org.piet.forumbackend.events.dtos.requests.CreateEventDto;
import org.piet.forumbackend.events.dtos.requests.EditEventDto;
import org.piet.forumbackend.events.dtos.requests.EventInviteResponseDto;
import org.piet.forumbackend.events.dtos.requests.InviteUsersDto;
import org.piet.forumbackend.events.dtos.responses.EventDto;
import org.piet.forumbackend.events.dtos.responses.ListEventDto;
import org.piet.forumbackend.events.dtos.responses.UserEventDto;
import org.piet.forumbackend.events.services.EventsService;
import org.piet.forumbackend.fishing_spots.core.exceptions.FishingSpotNotFoundException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${forum.api.prefix}/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Endpoints for event management")
public class EventController {
    private final EventsService eventsService;
    private final UserService userService;

    @PostMapping()
    public ResponseEntity<EventDto> createEvent(@Valid @RequestBody CreateEventDto dto) throws UserNotLoggedInException, FishingSpotNotFoundException {
        EventDto eventDto = EventDtoMapper.toEventDto(eventsService.createEvent(dto), userService.getCurrentUserOrNull());

        return ResponseEntity.status(HttpStatus.CREATED).body(eventDto);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEventById(@PathVariable Long eventId) throws NotFoundException {
        return ResponseEntity.ok(eventsService.getEventDtoById(eventId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ListEventDto>> getEventsByName(@RequestParam("q") String query){
        var dtos = eventsService.getEventsByName(query);

        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventDto> editEvent(@PathVariable Long eventId, @Valid @RequestBody EditEventDto dto) throws NotFoundException {
        return ResponseEntity.ok(EventDtoMapper.toEventDto(eventsService.editEvent(eventId, dto), userService.getCurrentUserOrNull()));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long eventId) throws UserNotLoggedInException, NotFoundException {
        eventsService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/invite")
    public ResponseEntity<?> inviteUserToEvent(@PathVariable Long eventId, @Valid @RequestBody InviteUsersDto dto) throws UserNotLoggedInException, NotFoundException {
        UUID currentUserId = userService.getCurrentUser().getId();
        for(UUID id : dto.getUserIds()){
            eventsService.inviteUser(eventId, id, currentUserId);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/response")
    public ResponseEntity<?> respondToInvite(@PathVariable Long eventId, @Valid @RequestBody EventInviteResponseDto dto) throws UserNotLoggedInException, NotFoundException {
        eventsService.respondToInvite(eventId, dto.getStatus());

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{eventId}/participants/{userId}")
    public ResponseEntity<?> removeParticipant(@PathVariable Long eventId, @PathVariable UUID userId) throws UserNotLoggedInException, NotFoundException {
        eventsService.removeUserFromEvent(eventId, userId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{eventId}/participants")
    public ResponseEntity<PageDto<UserEventDto>> getParticipants(@PathVariable Long eventId, PaginationDto pagination) throws NotFoundException {
        return ResponseEntity.ok(eventsService.getParticipants(eventId, pagination));
    }
}
