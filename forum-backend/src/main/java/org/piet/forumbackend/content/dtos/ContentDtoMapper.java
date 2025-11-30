package org.piet.forumbackend.content.dtos;

import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.content.entities.enums.VoteType;
import org.piet.forumbackend.users.core.dtos.ContentUserDto;

public class ContentDtoMapper {
    public static ContentDto toContentDto(Content content, VoteType userVote){
        return new ContentDto(
                content.getId(),
                new ContentUserDto(content.getAuthor().getUsername()),
                content.getContent(),
                content.getCreatedAt(),
                content.getContentType(),
                getParentDtoFromContent(content),
                content.getAttachedPhotos(),
                content.getEditHistory(),
                content.getRating(),
                userVote
        );
    }

    public static ContentDto toContentDto(Content content){
        return new ContentDto(
                content.getId(),
                new ContentUserDto(content.getAuthor().getUsername()),
                content.getContent(),
                content.getCreatedAt(),
                content.getContentType(),
                getParentDtoFromContent(content),
                content.getAttachedPhotos(),
                content.getEditHistory(),
                content.getRating(),
                VoteType.NO_VOTE
        );
    }

    public static ParentContentDto getParentDtoFromContent(Content content){
        return content.getParent() != null ? new ParentContentDto(content.getParent().getId()) : null;
    }
}
