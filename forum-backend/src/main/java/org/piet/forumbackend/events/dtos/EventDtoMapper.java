package org.piet.forumbackend.events.dtos;

import org.piet.forumbackend.events.dtos.responses.EventDto;
import org.piet.forumbackend.events.dtos.responses.EventParticipantDto;
import org.piet.forumbackend.events.dtos.responses.ListEventDto;
import org.piet.forumbackend.events.dtos.responses.UserEventDto;
import org.piet.forumbackend.events.entites.Event;
import org.piet.forumbackend.events.entites.UserEvent;
import org.piet.forumbackend.fishing_spots.dtos.FishingSpotListDto;
import org.piet.forumbackend.fishing_spots.dtos.responses.FishingSpotDto;
import org.piet.forumbackend.users.core.dtos.UsersDtoMapper;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.groups.dtos.UserGroupDtoMapper;

import java.util.stream.Collectors;

public class EventDtoMapper {
    public static ListEventDto toListEventDto(Event event) {
        return new ListEventDto(
                event.getId(),
                event.getName(),
                event.getStartsAt(),
                event.getEndsAt(),
                FishingSpotListDto.create(event.getLocation()),
                UserGroupDtoMapper.toListUserGroupDto(event.getGroup())
        );
    }

    public static UserEventDto toUserEventDto(UserEvent userEvent) {
        return new UserEventDto(
                toListEventDto(userEvent.getEvent()),
                UsersDtoMapper.toListUserDto(userEvent.getUser()),
                userEvent.getStatus()
        );
    }

    public static EventParticipantDto toEventParticipantDto(UserEvent userEvent) {
        return new EventParticipantDto(UsersDtoMapper.toListUserDto(userEvent.getUser()), userEvent.getStatus());
    }

    public static EventDto toEventDto(Event event) {
        return new EventDto(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getStartsAt(),
                event.getEndsAt(),
                FishingSpotDto.create(event.getLocation()),
                UsersDtoMapper.toListUserDto(event.getCreator()),
                UserGroupDtoMapper.toListUserGroupDto(event.getGroup()),
                event.getUserEvents().stream().map(EventDtoMapper::toEventParticipantDto).collect(Collectors.toSet()),
                false
        );
    }

    public static EventDto toEventDto(Event event, User currentUser) {
        if (currentUser == null)
            return toEventDto(event);
        return new EventDto(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getStartsAt(),
                event.getEndsAt(),
                FishingSpotDto.create(event.getLocation()),
                UsersDtoMapper.toListUserDto(event.getCreator()),
                UserGroupDtoMapper.toListUserGroupDto(event.getGroup()),
                event.getUserEvents().stream().map(EventDtoMapper::toEventParticipantDto).collect(Collectors.toSet()),
                event.getCreator().equalsUser(currentUser)
                        || event.getUserEvents()
                        .stream()
                        .anyMatch(ue -> ue.getUser().equalsUser(currentUser))
        );
    }
}
