package org.piet.forumbackend.content.dtos;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.content.entities.Content}
 */
@Value
public class ParentContentDto implements Serializable {
    Long id;
}