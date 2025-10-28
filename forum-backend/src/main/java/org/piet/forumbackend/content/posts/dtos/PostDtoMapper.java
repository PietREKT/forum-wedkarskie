package org.piet.forumbackend.content.posts.dtos;

import org.piet.forumbackend.content.posts.entities.Post;
import org.piet.forumbackend.users.dtos.UsersDtoMapper;

public class PostDtoMapper {
    public static PostDto toPostDto(Post post) {
        return new PostDto(
                UsersDtoMapper.toPostUserDto(post.getAuthor()),
                post.getContent(),
                post.getAttachedPhotos(),
                post.getEditHistory(),
                post.getRating(),
                post.getCreatedAt()
        );
    }
}
