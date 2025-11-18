package org.piet.forumbackend.content.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.users.entities.User;

import java.time.Instant;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
public class ContentBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(targetEntity = User.class)
    User author;

    String content;

    Instant createdAt;

    Long rating = 0L;

    public String toLogString() {
        return "{ " +
                "ID: " +
                id +
                ", createdAt: " +
                createdAt + " }";
    }
}
