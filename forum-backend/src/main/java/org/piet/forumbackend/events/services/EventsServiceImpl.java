package org.piet.forumbackend.events.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.piet.forumbackend.events.dtos.EventDtoMapper;
import org.piet.forumbackend.events.dtos.requests.CreateEventDto;
import org.piet.forumbackend.events.dtos.requests.EditEventDto;
import org.piet.forumbackend.events.dtos.responses.EventDto;
import org.piet.forumbackend.events.dtos.responses.ListEventDto;
import org.piet.forumbackend.events.dtos.responses.UserEventDto;
import org.piet.forumbackend.events.entites.AttendanceStatus;
import org.piet.forumbackend.events.entites.Event;
import org.piet.forumbackend.events.entites.UserEvent;
import org.piet.forumbackend.events.repositories.EventRepository;
import org.piet.forumbackend.events.repositories.UserEventRepository;
import org.piet.forumbackend.fishing_spots.entities.FishingSpot;
import org.piet.forumbackend.fishing_spots.exceptions.FishingSpotNotFoundException;
import org.piet.forumbackend.fishing_spots.services.FishingSpotService;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventsServiceImpl implements EventsService {
    private final UserService userService;
    private final UserGroupService userGroupService;
    private final FishingSpotService fishingSpotService;
    private final EventRepository eventRepository;
    private final MessageSource messageSource;
    private final UserEventRepository userEventRepository;

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
        User currentUser = userService.getCurrentUser();
        Set<UUID> invitedUserIds = eventDto.getInvitedUsersIds();
        Event event = new Event();
        event.setCreator(currentUser);
        event.setName(eventDto.getName());
        event.setDescription(eventDto.getDescription());
        event.setGroup(group);
        event.setLocation(spot);
        event.setStartsAt(eventDto.getStartsAt());
        event.setEndsAt(eventDto.getEndsAt());

        invitedUserIds.add(currentUser.getId());

        event.setUserEvents(
                invitedUserIds
                        .stream()
                        .map(userService::getUserById)
                        .map(u -> {
                            UserEvent userEvent = new UserEvent();
                            userEvent.setEvent(event);
                            userEvent.setStatus(u.equalsUser(currentUser) ?
                                    AttendanceStatus.CONFIRMED :
                                    AttendanceStatus.INVITED);
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
    public List<ListEventDto> getEventsByName(String query) {
        if(query == null) return List.of();

        String q = query.trim();

        if(q.length() < 2){
            return List.of();
        }

        return eventRepository.findTop10ByNameStartingWithIgnoreCaseOrderByNameAsc(q)
                .stream().map(EventDtoMapper::toListEventDto).toList();
    }

    @Override
    public PageDto<EventDto> getEventsAtSpot(Long spotId, PaginationDto pagination) {
        var page = eventRepository.findAllByLocationAndFuture(spotId,
                        Instant.now(),
                        pagination.toPageable())
                .map(e -> EventDtoMapper.toEventDto(e, userService.getCurrentUserOrNull()));
        return PageDto.of(page);
    }

    @Override
    public PageDto<EventDto> getEventsForGroup(UUID groupId, PaginationDto pagination) {
        var page = eventRepository.findAllByGroup_IdAndFuture(groupId, Instant.now(), pagination.toPageable())
                .map(e -> EventDtoMapper.toEventDto(e, userService.getCurrentUserOrNull()));

        return PageDto.of(page);
    }

    @Override
    public PageDto<EventDto> getEventsCreatedByUser(UUID userId, PaginationDto pagination) {
        var page = eventRepository.findAllByCreator_IdAndFuture(userId,
                Instant.now(),
                pagination.toPageable())
                .map(e -> EventDtoMapper.toEventDto(e, userService.getCurrentUserOrNull()));

        return PageDto.of(page);
    }

    @Override
    public PageDto<EventDto> getUpcomingEventsForUser(UUID userId, PaginationDto pagination) {
        var page = eventRepository.findAllByUserParticipatingAndStatus(userId,
                Instant.now(),
                List.of(AttendanceStatus.CONFIRMED, AttendanceStatus.MAYBE),
                pagination.toPageable())
                .map(e -> EventDtoMapper.toEventDto(e, userService.getCurrentUserOrNull()));
        return PageDto.of(page);
    }

    @Override
    public PageDto<EventDto> getUserInvites(UUID userId, PaginationDto pagination) {
        var page = eventRepository.findAllByUserParticipatingAndStatus(userId,
                        Instant.now(),
                        List.of(AttendanceStatus.INVITED),
                        pagination.toPageable())
                .map(e -> EventDtoMapper.toEventDto(e, userService.getCurrentUserOrNull()));
        return PageDto.of(page);
    }

    @Override
    @Transactional
    public void inviteUser(Long eventId, UUID userIdToInvite, UUID inviterId) throws NotFoundException {
        Event event = getEventById(eventId);
        UserEvent userEvent = new UserEvent();
        userEvent.setStatus(AttendanceStatus.INVITED);
        userEvent.setEvent(event);
        userEvent.setUser(userService.getUserById(userIdToInvite));
        event.addUserEvent(userEvent);
    }

    @Override
    @Transactional
    public void respondToInvite(Long eventId, AttendanceStatus status) throws UserNotLoggedInException, NotFoundException {
        User user = userService.getCurrentUser();
        Event event = getEventById(eventId);
        if (status == AttendanceStatus.INVITED){
            throw new BadRequestException("You can't reinvite yourself!");
        }
        if (status == AttendanceStatus.REJECTED){
            event.getUserEvents().removeIf(ue -> ue.getUser().equalsUser(user));
            return;
        }
        event.getUserEvents().stream()
                .filter(ue -> ue.getUser().equalsUser(user))
                .findFirst()
                .orElseThrow()
                .setStatus(status);
    }

    @Override
    @Transactional
    public void removeUserFromEvent(Long eventId, UUID userIdToRemove) throws UserNotLoggedInException, NotFoundException {
        User currentUser = userService.getCurrentUser();
        Event event = getEventById(eventId);
        if (!currentUser.isMod() && !event.getCreator().equalsUser(currentUser)){
            throw new AccessDeniedException("You can't remove users from events if you're not the creator of that event.");
        }
        boolean removed = event.getUserEvents().removeIf(ue -> ue.getUser().getId().equals(userIdToRemove));
        if (removed){
            log.info("{} removed user with id: {} from {}",
                    currentUser.toLogStringShort(),
                    userIdToRemove,
                    event.toLogStringShort());
        }
    }

    @Override
    @Transactional
    public PageDto<UserEventDto> getParticipants(Long eventId, PaginationDto pagination) throws NotFoundException {
        var userEvents = userEventRepository.findAllByEvent_Id(eventId, pagination.toPageable())
                .map(EventDtoMapper::toUserEventDto);
        return PageDto.of(userEvents);
    }
}
