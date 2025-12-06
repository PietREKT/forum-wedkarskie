package org.piet.forumbackend.events.services;

import org.piet.forumbackend.events.dtos.requests.CreateEventDto;
import org.piet.forumbackend.events.dtos.requests.EditEventDto;
import org.piet.forumbackend.events.dtos.responses.EventDto;
import org.piet.forumbackend.events.dtos.responses.UserEventDto;
import org.piet.forumbackend.events.entites.AttendanceStatus;
import org.piet.forumbackend.events.entites.Event;
import org.piet.forumbackend.fishing_spots.exceptions.FishingSpotNotFoundException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;

import java.util.UUID;

public interface EventsService {
    Event createEvent(CreateEventDto eventDto) throws UserNotLoggedInException, FishingSpotNotFoundException;

    Event editEvent(Long eventId, EditEventDto dto) throws NotFoundException;

    void deleteEvent(Long eventId) throws UserNotLoggedInException, NotFoundException;

    Event getEventById(Long eventId) throws NotFoundException;
    EventDto getEventDtoById(Long eventId) throws NotFoundException;

    PageDto<EventDto> getEventsAtSpot(Long spotId, PaginationDto pagination);
    PageDto<EventDto> getEventsForGroup(UUID groupId, PaginationDto pagination);
    PageDto<EventDto> getEventsCreatedByUser(UUID userId, PaginationDto pagination);
    PageDto<EventDto> getUpcomingEventsForUser(User user, PaginationDto pagination);
    PageDto<EventDto> getUpcomingEventsForUser(UUID userId, PaginationDto pagination);

    void inviteUser(Long eventId, Long userIdToInvite, Long inviterId);

    void respondToInvite(Long eventId, AttendanceStatus status);

    void removeUserFromEvent(Long eventId, Long userIdToRemove);

    PageDto<UserEventDto> getParticipants(Long eventId, PaginationDto pagination);

    /**
     *
     * @param userId ID of the user that should be queried
     * @return Event attendance for specified user.
     */
    PageDto<UserEventDto> getUserEvents(Long userId);

    /**
     *
     * @return Event Attendance for current user
     */
    PageDto<UserEventDto> getUserEvents();

}
