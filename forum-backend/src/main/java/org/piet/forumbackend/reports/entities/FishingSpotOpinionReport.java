package org.piet.forumbackend.reports.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.piet.forumbackend.fishing_spots.entities.FishingSpotOpinion;
import org.piet.forumbackend.reports.entities.enums.ReportReason;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
public class FishingSpotOpinionReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @ManyToOne
    @JoinColumn(name = "opinion_id")
    FishingSpotOpinion opinion;

    ReportReason reason;

    Instant createdAt;
}
