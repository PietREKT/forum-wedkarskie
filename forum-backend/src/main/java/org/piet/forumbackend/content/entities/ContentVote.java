package org.piet.forumbackend.content.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.content.entities.enums.VoteType;
import org.piet.forumbackend.users.entities.User;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "content_votes",
        uniqueConstraints = @UniqueConstraint(columnNames =
                {"content_id", "user_id"}
        )
)
public class ContentVote {
    @Id
    @GeneratedValue
    Long id;

    @ManyToOne
    @JoinColumn(name = "content_id")
    Content content;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;

    @Enumerated(EnumType.STRING)
    VoteType vote;

    Instant createdAt = Instant.now();

    public String toLogString() {
        return "{ " +
                "content id: " + content.getId() +
                ", voter id: " + user.getId() +
                ", vote: " + vote.name() +
                ", created at: " + DateTimeFormatter.ofPattern("dd.MM.yyyy '@' HH:mm:ss").withZone(ZoneId.systemDefault()).format(createdAt) +
                " }";
    }
}
