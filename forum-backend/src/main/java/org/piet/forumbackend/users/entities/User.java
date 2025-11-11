package org.piet.forumbackend.users.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.events.entites.UserEvent;
import org.piet.forumbackend.utils.RoleConverter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "app_users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true)
    String username;

    String name;

    String surname;

    String email;

    @JsonIgnore
    String password;

    String phone;

    Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Convert(converter = RoleConverter.class)
    Role role = Role.USER;

    @OneToMany
    @JoinColumn(name = "user_event_id")
    List<UserEvent> events;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.role.getAuthorities();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public boolean equals(User anotherUser) {
        return anotherUser.id.equals(this.id);
    }

    public boolean hasPermLevelAtLeast(Role other) {
        return role.hasAtLeast(other);
    }
}
