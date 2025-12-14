package org.piet.forumbackend.events.services;

import org.piet.forumbackend.events.dtos.requests.CreateEventDto;
import org.piet.forumbackend.events.dtos.requests.EditEventDto;
import org.piet.forumbackend.events.dtos.responses.EventDto;
import org.piet.forumbackend.events.dtos.responses.ListEventDto;
import org.piet.forumbackend.events.dtos.responses.UserEventDto;
import org.piet.forumbackend.events.entites.AttendanceStatus;
import org.piet.forumbackend.events.entites.Event;
import org.piet.forumbackend.fishing_spots.core.exceptions.FishingSpotNotFoundException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;

import java.util.List;
import java.util.UUID;

public interface EventsService {
    Event createEvent(CreateEventDto eventDto) throws UserNotLoggedInException, FishingSpotNotFoundException;

    Event editEvent(Long eventId, EditEventDto dto) throws NotFoundException;

    void deleteEvent(Long eventId) throws UserNotLoggedInException, NotFoundException;

    Event getEventById(Long eventId) throws NotFoundException;
    EventDto getEventDtoById(Long eventId) throws NotFoundException;

    public List<ListEventDto> getEventsByName(String query);

    PageDto<EventDto> getEventsAtSpot(Long spotId, PaginationDto pagination);
    PageDto<EventDto> getEventsForGroup(UUID groupId, PaginationDto pagination);
    PageDto<EventDto> getEventsCreatedByUser(UUID userId, PaginationDto pagination);
    default PageDto<EventDto> getUpcomingEventsForUser(User user, PaginationDto pagination){
        return getUpcomingEventsForUser(user.getId(), pagination);
    }
    PageDto<EventDto> getUpcomingEventsForUser(UUID userId, PaginationDto pagination);

    PageDto<EventDto> getUserInvites(UUID userId, PaginationDto pagination);

    void inviteUser(Long eventId, UUID userIdToInvite, UUID inviterId) throws NotFoundException;

    void respondToInvite(Long eventId, AttendanceStatus status) throws UserNotLoggedInException, NotFoundException;

    void removeUserFromEvent(Long eventId, UUID userIdToRemove) throws UserNotLoggedInException, NotFoundException;

    PageDto<UserEventDto> getParticipants(Long eventId, PaginationDto pagination) throws NotFoundException;

    void leave(Long eventId) throws UserNotLoggedInException;
}
