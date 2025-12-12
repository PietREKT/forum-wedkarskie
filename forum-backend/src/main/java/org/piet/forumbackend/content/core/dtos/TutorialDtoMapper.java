package org.piet.forumbackend.content.core.dtos;

import org.piet.forumbackend.content.core.dtos.responses.tutorials.ListTutorialDto;
import org.piet.forumbackend.content.core.dtos.responses.tutorials.TutorialDto;
import org.piet.forumbackend.content.core.entities.Tutorial;
import org.piet.forumbackend.fish.dtos.FishDtoMapper;
import org.piet.forumbackend.users.core.dtos.UsersDtoMapper;

import java.util.stream.Collectors;

public class TutorialDtoMapper {
    public static TutorialDto toTutorialDto(Tutorial tutorial){
        return new TutorialDto(
                UsersDtoMapper.toUserDto(tutorial.getAuthor()),
                tutorial.getMethods(),
                tutorial.getFishMentioned().stream().map(FishDtoMapper::toFishListDto).collect(Collectors.toSet()),
                ContentDtoMapper.toContentDto(tutorial)
        );
    }

    public static ListTutorialDto toListTutorialDto(Tutorial tutorial){
        return new ListTutorialDto(
                tutorial.getId(),
                tutorial.getTitle(),
                tutorial.getRating()
        );
    }
}
