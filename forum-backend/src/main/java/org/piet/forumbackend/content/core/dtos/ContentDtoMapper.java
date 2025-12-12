package org.piet.forumbackend.content.core.dtos;

import org.piet.forumbackend.content.core.dtos.responses.content.ContentDto;
import org.piet.forumbackend.content.core.dtos.responses.content.ParentContentDto;
import org.piet.forumbackend.content.core.entities.Content;
import org.piet.forumbackend.content.core.entities.ContentVote;
import org.piet.forumbackend.content.core.entities.enums.VoteType;
import org.piet.forumbackend.users.core.dtos.UsersDtoMapper;
import org.piet.forumbackend.users.core.entities.User;

import java.util.List;

public class ContentDtoMapper {
    public static ContentDto toContentDto(Content content, List<ContentVote> votes, User currentUser){
        return new ContentDto(
                content.getId(),
                UsersDtoMapper.toListUserDto(content.getAuthor()),
                content.getContent(),
                content.getCreatedAt(),
                content.getContentType(),
                getParentDtoFromContent(content),
                content.getAttachedPhotos(),
                content.getEditHistory(),
                content.getRating(),
                votes.stream()
                        .filter(v -> v.getUser().equalsUser(currentUser))
                        .findFirst()
                        .map(ContentVote::getVote)
                        .orElse(VoteType.NO_VOTE)
        );
    }

    public static ContentDto toContentDto(Content content){
        return new ContentDto(
                content.getId(),
                UsersDtoMapper.toListUserDto(content.getAuthor()),
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
