package org.piet.forumbackend.pagination;

import java.io.Serializable;
@lombok.Value
public class PaginationDto implements Serializable {
    Integer page;
    Integer size;

    public PaginationDto(Integer page, Integer size) {
        this.page = (page != null && page >= 0) ? page : 0;
        this.size = (size != null && size > 0) ? size : 20;
    }
}
