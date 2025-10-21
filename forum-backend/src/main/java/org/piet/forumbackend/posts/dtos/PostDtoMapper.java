package org.piet.forumbackend.posts.dtos;

import org.piet.forumbackend.posts.entities.Comment;
import org.piet.forumbackend.posts.entities.Post;
import org.piet.forumbackend.users.dtos.UsersDtoMapper;

import java.util.List;

public class PostDtoMapper {
    public static PostDto toPostDto(Post post) {
        return new PostDto(
                UsersDtoMapper.toPostUserDto(post.getAuthor()),
                post.getContent(),
                post.getAttachedPhotos(),
                post.getEditHistory(),
                post.getRating(),
                post.getPostedAt()
        );
    }

    public static PostWithCommentsDto toPostWithCommentsDto(Post post, List<Comment> comments) {
        return new PostWithCommentsDto(
                post.getId(),
                UsersDtoMapper.toPostUserDto(post.getAuthor()),
                post.getContent(),
                post.getAttachedPhotos(),
                post.getEditHistory(),
                post.getRating(),
                post.getPostedAt(),
                comments.stream().map(PostDtoMapper::toCommentDto).toList()
        );
    }

    public static CommentDto toCommentDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getContent(), comment.getCreatedAt());
    }
}
