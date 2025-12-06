package org.piet.forumbackend.events.services;

import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.events.dtos.EventDtoMapper;
import org.piet.forumbackend.events.dtos.requests.CreateEventDto;
import org.piet.forumbackend.events.dtos.requests.EditEventDto;
import org.piet.forumbackend.events.dtos.responses.EventDto;
import org.piet.forumbackend.events.dtos.responses.UserEventDto;
import org.piet.forumbackend.events.entites.AttendanceStatus;
import org.piet.forumbackend.events.entites.Event;
import org.piet.forumbackend.events.entites.UserEvent;
import org.piet.forumbackend.events.repositories.EventRepository;
import org.piet.forumbackend.fishing_spots.entities.FishingSpot;
import org.piet.forumbackend.fishing_spots.exceptions.FishingSpotNotFoundException;
import org.piet.forumbackend.fishing_spots.services.FishingSpotService;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserService;
import org.piet.forumbackend.users.groups.entities.UserGroup;
import org.piet.forumbackend.users.groups.services.UserGroupService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventsServiceImpl implements EventsService {
    private final UserService userService;
    private final UserGroupService userGroupService;
    private final FishingSpotService fishingSpotService;
    private final EventRepository eventRepository;
    private final MessageSource messageSource;

    private void checkEventAuthor(Long eventId, User currentUser) throws NotFoundException {
        Event e = getEventById(eventId);
        checkEventAuthor(e, currentUser);
    }

    private void checkEventAuthor(Event event, User currentUser) {
        if (!event.getCreator().equalsUser(currentUser)) {
            throw new AccessDeniedException(
                    messageSource.getMessage("error.events.not_author",
                            null,
                            LocaleContextHolder.getLocale()
                    )
            );
        }
    }

    @Override
    @Transactional
    public Event createEvent(CreateEventDto eventDto) throws UserNotLoggedInException, FishingSpotNotFoundException {
        UserGroup group = userGroupService.getByIdOpt(eventDto.getGroupId()).orElse(null);
        FishingSpot spot = fishingSpotService.getFishingSpotById(eventDto.getLocationId());
        Event event = new Event();
        event.setCreator(userService.getCurrentUser());
        event.setName(eventDto.getName());
        event.setDescription(eventDto.getDescription());
        event.setGroup(group);
        event.setLocation(spot);
        event.setStartsAt(eventDto.getStartsAt());
        event.setEndsAt(eventDto.getEndsAt());

        event.setUserEvents(
                eventDto.getInvitedUsersIds()
                        .stream()
                        .map(userService::getUserByIdOpt)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .map(u -> {
                            UserEvent userEvent = new UserEvent();
                            userEvent.setEvent(event);
                            userEvent.setStatus(AttendanceStatus.INVITED);
                            userEvent.setUser(u);
                            return userEvent;
                        })
                        .collect(Collectors.toSet())
        );

        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public Event editEvent(Long eventId, EditEventDto dto) throws NotFoundException {
        Event event = getEventById(eventId);

        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }
        if (dto.getName() != null) {
            event.setName(dto.getName());
        }
        if (dto.getLocationId() != null) {
            event.setLocation(fishingSpotService.getFishingSpotById(dto.getLocationId()));
        }
        if (dto.getStartsAt() != null) {
            event.setStartsAt(dto.getStartsAt());
        }
        if (dto.getEndsAt() != null) {
            event.setEndsAt(dto.getEndsAt());
        }

        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public void deleteEvent(Long eventId) throws UserNotLoggedInException, NotFoundException {
        if (!eventRepository.existsById(eventId)) return;
        checkEventAuthor(eventId, userService.getCurrentUser());
        eventRepository.deleteById(eventId);
    }

    @Override
    public Event getEventById(Long eventId) throws NotFoundException {
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException(
                messageSource.getMessage("error.events.not_found",
                        new Object[]{eventId},
                        LocaleContextHolder.getLocale()
                )
        ));
    }

    @Override
    public EventDto getEventDtoById(Long eventId) throws NotFoundException {
        return EventDtoMapper.toEventDto(getEventById(eventId));
    }


    @Override
    public PageDto<EventDto> getEventsAtSpot(Long spotId, PaginationDto pagination) {
        var page = eventRepository.findAllByLocation_Id(spotId, pagination.toPageable(Sort.by()));
    }

    @Override
    public PageDto<EventDto> getEventsForGroup(UUID groupId, PaginationDto pagination) {
        return null;
    }

    @Override
    public PageDto<EventDto> getEventsCreatedByUser(UUID userId, PaginationDto pagination) {
        return null;
    }

    @Override
    public PageDto<EventDto> getUpcomingEventsForUser(User user, PaginationDto pagination) {
        return null;
    }

    @Override
    public PageDto<EventDto> getUpcomingEventsForUser(UUID userId, PaginationDto pagination) {
        return null;
    }

    @Override
    @Transactional
    public void inviteUser(Long eventId, Long userIdToInvite, Long inviterId) {

    }

    @Override
    @Transactional
    public void respondToInvite(Long eventId, AttendanceStatus status) {

    }

    @Override
    @Transactional
    public void removeUserFromEvent(Long eventId, Long userIdToRemove) {

    }

    @Override
    @Transactional
    public PageDto<UserEventDto> getParticipants(Long eventId, PaginationDto pagination) {
        return null;
    }

    @Override
    @Transactional
    public PageDto<UserEventDto> getUserEvents(Long userId) {
        return null;
    }

    @Override
    @Transactional
    public PageDto<UserEventDto> getUserEvents() {
        return null;
    }
}
