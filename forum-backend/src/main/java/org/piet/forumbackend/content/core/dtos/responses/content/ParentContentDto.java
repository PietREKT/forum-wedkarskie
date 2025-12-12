package org.piet.forumbackend.content.core.dtos.responses.content;

import lombok.Value;
import org.piet.forumbackend.content.core.entities.Content;

import java.io.Serializable;

/**
 * DTO for {@link Content}
 */
@Value
public class ParentContentDto implements Serializable {
    Long id;
}