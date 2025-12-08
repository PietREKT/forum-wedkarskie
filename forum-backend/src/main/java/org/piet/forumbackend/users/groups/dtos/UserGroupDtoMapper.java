package org.piet.forumbackend.users.groups.dtos;

import org.piet.forumbackend.users.groups.dtos.responses.ListUserGroupDto;
import org.piet.forumbackend.users.groups.dtos.responses.UserGroupDto;
import org.piet.forumbackend.users.groups.entities.UserGroup;

public class UserGroupDtoMapper {
    public static UserGroupDto toUserGroupDto(UserGroup userGroup){
        return UserGroupDto.create(userGroup);
    }
    public static ListUserGroupDto toListUserGroupDto(UserGroup userGroup){
        return new ListUserGroupDto(
                userGroup.getId(),
                userGroup.getName(),
                userGroup.getMembers().size()
        );
    }
}
