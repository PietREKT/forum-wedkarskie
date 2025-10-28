package org.piet.forumbackend.content.comments.dtos;

import org.piet.forumbackend.content.comments.Comment;
import org.piet.forumbackend.users.dtos.UsersDtoMapper;

public class CommentDtoMapper {
    public static CommentDto toCommentDto(Comment comment) {
        return new CommentDto(comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                UsersDtoMapper.toPostUserDto(comment.getAuthor()));
    }
}
