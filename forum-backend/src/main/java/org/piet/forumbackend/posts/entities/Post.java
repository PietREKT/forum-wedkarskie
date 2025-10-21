package org.piet.forumbackend.posts.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.users.entities.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    @ElementCollection
            @CollectionTable(name = "post_photos", joinColumns = @JoinColumn(name = "post_id"))
            @Column(name = "photo_url")
    List<String> attachedPhotos = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "post_edit_history",
            joinColumns = @JoinColumn(name = "post_id")
    )
    @MapKeyColumn(name = "edited_at")
    @Column(name = "edited_content", columnDefinition = "TEXT")
    HashMap<Instant, String> editHistory;

    Long rating = 0L;

    Instant postedAt = Instant.now();
}
