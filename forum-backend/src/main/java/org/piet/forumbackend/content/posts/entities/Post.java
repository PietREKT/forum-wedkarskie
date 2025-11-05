package org.piet.forumbackend.content.posts.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.content.ContentBase;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Post extends ContentBase {

    @ElementCollection
    @CollectionTable(name = "post_photos", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "photo_url")
    List<String> attachedPhotos = new ArrayList<>();

    @PrePersist
    private void onCreate() {
        super.initEditHistory();
    }
}
