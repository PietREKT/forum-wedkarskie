package org.piet.forumbackend.globals.utils;

import jakarta.persistence.AttributeConverter;
import org.piet.forumbackend.users.core.entities.Role;

public class RoleConverter implements AttributeConverter<Role, String> {
    @Override
    public String convertToDatabaseColumn(Role role) {
        return role.name();
    }

    @Override
    public Role convertToEntityAttribute(String s) {
        return Role.valueOf(s);
    }
}
