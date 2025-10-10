package org.piet.forumbackend.posts.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String content;

    Instant createdAt = Instant.now();

    @ManyToOne(targetEntity = Post.class)
    Post post;
}
