package org.piet.forumbackend.users.core.dtos;

import org.piet.forumbackend.users.core.dtos.responses.*;
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
                user.getProfilePicUrl(),
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
        if (user == null) return null;
        return new ListUserDto(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getProfilePicUrl()
        );
    }

    public static PunishedUserListDto toPunishedUserListDto(User user){
        return new PunishedUserListDto(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getBannedUntil(),
                user.getMutedUntil(),
                user.getBanReason(),
                user.getProfilePicUrl()
        );
    }

    public static RoleDto toRoleDto(Role role){
        return new RoleDto(role.name());
    }
}
