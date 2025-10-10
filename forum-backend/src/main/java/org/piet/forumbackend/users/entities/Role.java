package org.piet.forumbackend.users.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "app_roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    public GrantedAuthority getAsAuthority(){
        return new SimpleGrantedAuthority(this.name);
    }
}
