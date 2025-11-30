package org.piet.forumbackend.globals.pagination;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
@lombok.Value
public class PaginationDto implements Serializable {
    Integer page;
    Integer size;

    public PaginationDto(Integer page, Integer size) {
        this.page = (page != null && page >= 0) ? page : 0;
        this.size = (size != null && size > 0) ? size : 20;
    }

    public Pageable toPageable() {
        return PageRequest.of(page, size);
    }

    public Pageable toPageable(Sort sort){
        return PageRequest.of(page, size, sort);
    }
}
