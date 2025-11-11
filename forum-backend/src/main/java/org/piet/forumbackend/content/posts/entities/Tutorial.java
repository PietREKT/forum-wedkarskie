package org.piet.forumbackend.content.posts.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.piet.forumbackend.content.VerificationStatus;

@Entity
public class Tutorial extends Post{
    String title;
    String rejectReason;

    @Enumerated(EnumType.STRING)
    VerificationStatus status = VerificationStatus.IN_REVIEW;
}
