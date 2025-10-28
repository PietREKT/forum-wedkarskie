package org.piet.forumbackend.content;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.users.entities.User;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

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

    @ElementCollection
    @CollectionTable(
            name = "content_edit_history",
            joinColumns = @JoinColumn(name = "post_id")
    )
    @MapKeyColumn(name = "edited_at")
    @Column(name = "edited_content", columnDefinition = "TEXT")
    Map<Instant, String> editHistory;

    Instant createdAt;

    Long rating = 0L;

    protected void initEditHistory(){
        Instant now = Instant.now();
        editHistory = new HashMap<>();
        editHistory.put(now, content);
        createdAt = now;
    }
}
