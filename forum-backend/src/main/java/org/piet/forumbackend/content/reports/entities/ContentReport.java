package org.piet.forumbackend.content.reports.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.content.reports.ReportReason;
import org.piet.forumbackend.users.entities.User;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
public class ContentReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant createdAt;

    @Enumerated(value = EnumType.STRING)
    private ReportReason reason;

    protected void setCreatedAt() {
        createdAt = Instant.now();
    }

    @ManyToOne
    User reportedBy; //Do not show this to anyone, it's a hook for notifications
}
