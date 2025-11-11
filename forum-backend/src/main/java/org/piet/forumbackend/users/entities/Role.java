package org.piet.forumbackend.users.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.List;

public enum Role {
    USER(0, true),
    PZW(1, false),
    MOD(2, true),
    ADMIN(3, true),
    ROOT(4, true);

    private final int permLevel;
    private final boolean includeInPermissionTree;

    Role(int permLevel, boolean includeInPermissionTree) {
        this.permLevel = permLevel;
        this.includeInPermissionTree = includeInPermissionTree;
    }

    public boolean hasAtLeast(Role other){
        return this.permLevel >= other.permLevel;
    }

    public GrantedAuthority getAsAuthority(){
        return new SimpleGrantedAuthority("ROLE_" + this.name());
    }

    public List<GrantedAuthority> getAuthorities(){
        return Arrays.stream(Role.values())
                .filter(r -> r.permLevel <= this.permLevel)
                .filter(r -> r.includeInPermissionTree)
                .map(Role::getAsAuthority)
                .toList();
    }
}
