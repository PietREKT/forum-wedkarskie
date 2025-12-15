package org.piet.forumbackend.users.groups.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.piet.forumbackend.users.core.entities.User;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
public class UserGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private User owner;

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
            name = "user_group_admins",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> admins = new HashSet<>();

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
            name = "user_group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })
    @JoinTable(
            name = "user_group_candidates",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> memberCandidates = new HashSet<>();

    @NotBlank
    @Length(min = 3, max = 50)
    String name;

    Instant createdAt;

    @PrePersist
    @PreUpdate
    void validateOwnerIsMember() {
        if (createdAt == null)
            createdAt = Instant.now();

        if (owner != null && !members.contains(owner)) {
            addMember(owner);
        }
    }

    public void addMemberCandidate(User user){
        memberCandidates.add(user);
    }

    public void removeMemberCandidate(User user){
        memberCandidates.remove(user);
    }

    public void addMember(User user) {
        members.add(user);
        memberCandidates.remove(user);
    }

    public void removeMember(User user) {
        members.remove(user);
    }

    public boolean isMember(User user){
        return members.contains(user) || admins.contains(user);
    }

    public void addAdmin(User user){
        admins.add(user);
    }

    public void removeAdmin(User user){
        admins.remove(user);
    }

    public boolean isAdmin(User user){
        return admins.contains(user) || isOwner(user);
    }

    public boolean isOwner(User user){
        return owner.equalsUser(user);
    }

    public String toLogStringShort(){
        return "UserGroup { Id: \"" +
                id +
                "\", name: " +
                name +
                "\" }";
    }
}
