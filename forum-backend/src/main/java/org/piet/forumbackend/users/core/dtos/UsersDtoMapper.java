package org.piet.forumbackend.users.core.dtos;

import org.piet.forumbackend.users.core.dtos.responses.ContentUserDto;
import org.piet.forumbackend.users.core.dtos.responses.ListUserDto;
import org.piet.forumbackend.users.core.dtos.responses.RoleDto;
import org.piet.forumbackend.users.core.dtos.responses.UserDto;
import org.piet.forumbackend.users.core.entities.Role;
import org.piet.forumbackend.users.core.entities.User;

import java.util.stream.Collectors;

public class UsersDtoMapper {
    public static UserDto toUserDto(User user){
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getPhone(),
                user.getRole() != null ? UsersDtoMapper.toRoleDto(user.getRole()) : null,
                user.getFriends().stream().map(UsersDtoMapper::toListUserDto).collect(Collectors.toSet())
        );
    }

    public static ContentUserDto toPostUserDto(User user){
        return new ContentUserDto(
                user.getUsername(),
                user.getId()
        );
    }

    public static ListUserDto toListUserDto(User user){
        return new ListUserDto(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname()
        );
    }

    public static RoleDto toRoleDto(Role role){
        return new RoleDto(role.name());
    }
}
