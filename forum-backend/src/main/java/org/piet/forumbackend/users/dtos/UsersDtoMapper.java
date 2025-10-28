package org.piet.forumbackend.users.dtos;

import org.piet.forumbackend.users.entities.Role;
import org.piet.forumbackend.users.entities.User;

public class UsersDtoMapper {
    public static UserDto toUserDto(User user){
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getPhone(),
                user.getRole() != null ? UsersDtoMapper.toRoleDto(user.getRole()) : null
        );
    }

    public static PostUserDto toPostUserDto(User user){
        return new PostUserDto(
                user.getUsername()
        );
    }

    public static RoleDto toRoleDto(Role role){
        return new RoleDto(role.name());
    }
}
