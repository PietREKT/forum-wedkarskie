package org.piet.forumbackend.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "forum.constants.pagination")
@Component
@Getter
@Setter
public class PaginationProperties {
    private Integer defaultSize;
    private Integer defaultPage;
}
