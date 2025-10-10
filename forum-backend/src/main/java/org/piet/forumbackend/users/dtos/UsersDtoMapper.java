package org.piet.forumbackend.users.dtos;

import org.piet.forumbackend.users.entities.Role;
import org.piet.forumbackend.users.entities.User;

import java.util.ArrayList;

public class UsersDtoMapper {
    public static UserDto toUserDto(User user){
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getPhone(),
                user.getRoles() != null  ? user.getRoles().stream().map(UsersDtoMapper::toRoleDto).toList() : new ArrayList<>()
        );
    }

    public static RoleDto toRoleDto(Role role){
        return new RoleDto(role.getName());
    }
}
