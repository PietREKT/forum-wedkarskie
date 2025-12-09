package org.piet.forumbackend.users.core.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.piet.forumbackend.events.entites.UserEvent;
import org.piet.forumbackend.fishing_spots.entities.FishingSpot;
import org.piet.forumbackend.globals.utils.RoleConverter;
import org.piet.forumbackend.users.friends.entities.FriendRequest;
import org.piet.forumbackend.users.groups.entities.UserGroup;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "app_users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(unique = true)
    @Length(min = 5, max = 20)
    String username;

    @Length(min = 2, max = 30)
    String name;

    @Length(min = 2, max = 30)
    String surname;

    @Email
    String email;

    @JsonIgnore
    String password;

    String phone;

    Instant createdAt;
    Instant bannedUntil;
    Instant mutedUntil;
    String banReason;

    @Enumerated(EnumType.STRING)
    @Convert(converter = RoleConverter.class)
    Role role = Role.USER;

    @OneToMany
    @JoinColumn(name = "user_event_id")
    Set<UserEvent> events = new HashSet<>();

    @ManyToMany(mappedBy = "members")
    private Set<UserGroup> groups = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_friends",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "friend_id")
    )
    Set<User> friends = new HashSet<>();

    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<FriendRequest> sentRequests = new HashSet<>();
    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<FriendRequest> receivedRequests = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_favourite_spots",
        joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "spot_id")
    )
    Set<FishingSpot> favourites = new HashSet<>();

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
        return !isBanned(Instant.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    public boolean equalsUser(User anotherUser) {
        if (anotherUser == null) return false;
        return anotherUser.id.equals(this.id);
    }

    public boolean hasPermLevelAtLeast(Role other) {
        return role.hasPermsAtLeast(other);
    }

    @Override
    public String toString() {
        return "{ " +
                "username: " + username +
                ", role: " + role.name() +
                "}";
    }

    public String toLogStringShort() {
        return "User { Id: \"" +
                id +
                "\", username: " +
                username +
                "\" }";
    }

    @PrePersist
    void init() {
        createdAt = Instant.now();
    }

    public boolean isAdmin() {
        return hasPermLevelAtLeast(Role.ADMIN);
    }

    public boolean isMod() {
        return hasPermLevelAtLeast(Role.MOD);
    }

    public boolean isBanned(Instant now) {
        return bannedUntil != null && bannedUntil.isAfter(now);
    }

    public boolean isMuted(Instant now) {
        return mutedUntil != null && mutedUntil.isAfter(now);
    }

    public void addFavouriteFishingSpot(FishingSpot spot){
        favourites.add(spot);
    }
    public void removeFavouriteFishingSpot(FishingSpot spot){
        favourites.remove(spot);
    }
}
