package org.piet.forumbackend.posts.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.users.entities.User;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(targetEntity = User.class)
    User author;

    String content;

    Instant postedAt = Instant.now();
}
