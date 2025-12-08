package org.piet.forumbackend.reports.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.reports.entities.enums.ReportReason;
import org.piet.forumbackend.users.core.entities.User;

import java.time.Instant;

@Entity
@Getter @Setter @NoArgsConstructor
public class ContentReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant createdAt;

    @Enumerated(value = EnumType.STRING)
    private ReportReason reason;

    @PrePersist
    private void setCreatedAt() {
        createdAt = Instant.now();
    }

    @ManyToOne(optional = false)
            @JoinColumn(name = "content_id", nullable = false)
    Content reported;

    @ManyToOne(optional = false)
    User reportedBy; //Do not show this to anyone, it's a hook for notifications
}
