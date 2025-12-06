package org.piet.forumbackend.events.dtos;

import org.piet.forumbackend.events.dtos.responses.EventDto;
import org.piet.forumbackend.events.dtos.responses.EventParticipantDto;
import org.piet.forumbackend.events.dtos.responses.ListEventDto;
import org.piet.forumbackend.events.dtos.responses.UserEventDto;
import org.piet.forumbackend.events.entites.Event;
import org.piet.forumbackend.events.entites.UserEvent;
import org.piet.forumbackend.fishing_spots.dtos.FishingSpotDto;
import org.piet.forumbackend.users.core.dtos.UsersDtoMapper;
import org.piet.forumbackend.users.groups.dtos.responses.UserGroupDto;

import java.util.stream.Collectors;

public class EventDtoMapper {
    public static ListEventDto toListEventDto(Event event){
        return new ListEventDto(
                event.getId(),
                event.getName(),
                event.getStartsAt(),
                event.getEndsAt(),
                FishingSpotDto.create(event.getLocation()),
                UserGroupDto.create(event.getGroup())
        );
    }

    public static UserEventDto toUserEventDto(UserEvent userEvent){
        return new UserEventDto(
                toListEventDto(userEvent.getEvent()),
                UsersDtoMapper.toListUserDto(userEvent.getUser()),
                userEvent.getStatus()
        );
    }

    public static EventParticipantDto toEventParticipantDto(UserEvent userEvent){
        return new EventParticipantDto(UsersDtoMapper.toListUserDto(userEvent.getUser()), userEvent.getStatus());
    }

    public static EventDto toEventDto(Event event){
        return new EventDto(
                event.getId(),
                event.getName(),
                event.getDescription(),
                event.getStartsAt(),
                event.getEndsAt(),
                FishingSpotDto.create(event.getLocation()),
                UsersDtoMapper.toUserDto(event.getCreator()),
                UserGroupDto.create(event.getGroup()),
                event.getUserEvents().stream().map(EventDtoMapper::toEventParticipantDto).collect(Collectors.toSet())
        );
    }
}
