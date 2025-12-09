package org.piet.forumbackend.content.dtos;

import org.piet.forumbackend.content.dtos.responses.content.ContentDto;
import org.piet.forumbackend.content.dtos.responses.content.ParentContentDto;
import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.content.entities.ContentVote;
import org.piet.forumbackend.content.entities.enums.VoteType;
import org.piet.forumbackend.users.core.dtos.responses.ContentUserDto;
import org.piet.forumbackend.users.core.entities.User;

import java.util.List;

public class ContentDtoMapper {
    public static ContentDto toContentDto(Content content, List<ContentVote> votes, User currentUser){
        return new ContentDto(
                content.getId(),
                new ContentUserDto(content.getAuthor().getUsername(), content.getAuthor().getId()),
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
                new ContentUserDto(content.getAuthor().getUsername(), content.getAuthor().getId()),
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
