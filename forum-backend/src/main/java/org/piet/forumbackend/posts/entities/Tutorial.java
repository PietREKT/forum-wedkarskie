package org.piet.forumbackend.posts.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Tutorial extends Post{
    String title;
    String rejectReason;

    @Enumerated(EnumType.STRING)
    TUTORIAL_STATUS status = TUTORIAL_STATUS.IN_REVIEW;

    public enum TUTORIAL_STATUS {
        IN_REVIEW,
        ACCEPTED,
        REJECTED
    }
}
