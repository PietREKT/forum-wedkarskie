package org.piet.forumbackend.users.core.dtos;

import org.piet.forumbackend.users.core.entities.Role;
import org.piet.forumbackend.users.core.entities.User;

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

    public static ContentUserDto toPostUserDto(User user){
        return new ContentUserDto(
                user.getUsername()
        );
    }

    public static RoleDto toRoleDto(Role role){
        return new RoleDto(role.name());
    }
}
