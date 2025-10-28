package org.piet.forumbackend.users.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
public enum Role {
    USER(0),
    MOD(1),
    ADMIN(2),
    ROOT(3);

    private final int permLevel;

    Role(int permLevel) {
        this.permLevel = permLevel;
    }

    public boolean hasAtLeast(Role other){
        return this.permLevel >= other.permLevel;
    }

    public GrantedAuthority getAsAuthority(){
        return new SimpleGrantedAuthority("ROLE_" + this.name());
    }
}
