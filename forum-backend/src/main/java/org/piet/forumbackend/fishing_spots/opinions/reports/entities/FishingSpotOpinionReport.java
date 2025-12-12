package org.piet.forumbackend.fishing_spots.opinions.reports.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.piet.forumbackend.content.reports.entites.enums.ReportReason;
import org.piet.forumbackend.fishing_spots.opinions.core.entities.FishingSpotOpinion;

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
