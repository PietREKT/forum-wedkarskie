package org.piet.forumbackend.users.groups.dtos.responses;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;
import org.hibernate.validator.constraints.Length;
import org.piet.forumbackend.users.core.dtos.UsersDtoMapper;
import org.piet.forumbackend.users.core.dtos.responses.ListUserDto;
import org.piet.forumbackend.users.core.dtos.responses.UserDto;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.groups.entities.UserGroup;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO for {@link org.piet.forumbackend.users.groups.entities.UserGroup}
 */
@Value
public class UserGroupDto implements Serializable {
    UUID id;
    ListUserDto owner;
    Set<UserDto> admins;
    Set<UserDto> members;
    @NotBlank
    @Length(min = 3, max = 50)
    String name;
    String myRole;

    public static UserGroupDto create(UserGroup group){
       return create(group, null);
    }

    public static UserGroupDto create(UserGroup group, User currentUser){
        String userRole = null;

        if (currentUser != null){
            if (group.isOwner(currentUser)) userRole = "OWNER";
            else if (group.isAdmin(currentUser)) userRole = "ADMIN";
            else if (group.isMember(currentUser)) userRole = "MEMBER";
        }

        if (group == null) return null;
        return new UserGroupDto(
                group.getId(),
                UsersDtoMapper.toListUserDto(group.getOwner()),
                group.getAdmins().stream().map(UsersDtoMapper::toUserDto).collect(Collectors.toSet()),
                group.getMembers().stream().map(UsersDtoMapper::toUserDto).collect(Collectors.toSet()),
                group.getName(),
                userRole
        );
    }
}