package org.piet.forumbackend.content.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.piet.forumbackend.content.entities.enums.VerificationStatus;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fish.entities.enums.FishingMethod;
import org.piet.forumbackend.users.core.entities.User;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "tutorial")
@PrimaryKeyJoinColumn(name = "id")
public class Tutorial extends Content {

    @NotBlank
    String title;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.IN_REVIEW;

    @ManyToOne
    private User verifiedBy;

    String rejectionReason;

    @ElementCollection
    @CollectionTable(name = "methods_tutorials",
            joinColumns = @JoinColumn(name = "tutorial_id")
    )
    @Enumerated(EnumType.STRING)
            @Column(name = "methods")
    Set<FishingMethod> methods = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "fish_tutorials",
            joinColumns = @JoinColumn(name = "tutorial_id"),
            inverseJoinColumns = @JoinColumn(name = "fish_id")
    )
    Set<Fish> fishMentioned = new HashSet<>();
}