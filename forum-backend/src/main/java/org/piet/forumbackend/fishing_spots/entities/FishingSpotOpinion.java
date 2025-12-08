package org.piet.forumbackend.fishing_spots.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.piet.forumbackend.users.core.entities.User;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "fishing_spots_opinions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "spot_id"})
        }
)
public class FishingSpotOpinion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Max(5)
    @Min(1)
    private Integer rating;

    private String comment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @ManyToOne
    @JoinColumn(name = "spot_id", nullable = false)
    private FishingSpot spot;

    Instant createdAt;
}
