package org.piet.forumbackend.content.posts.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.content.ContentBase;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Post extends ContentBase {

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
    Map<Instant, String> editHistory;

    @PrePersist
    private void onCreate() {
        Instant now = Instant.now();
        editHistory = new HashMap<>();
        editHistory.put(now, super.getContent());
        super.setCreatedAt(now);
    }

    @Override
    public String toLogString() {
        String superString = super.toLogString();
        return superString
                .substring(0, superString.length() - 2) +
                ", attachedPhotos: " + attachedPhotos.size() +
                " }";
    }
}
