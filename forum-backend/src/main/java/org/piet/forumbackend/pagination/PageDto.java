package org.piet.forumbackend.pagination;

import lombok.Value;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@Value
public class PageDto<T> implements Serializable {
    boolean last;
    long totalElements;
    long totalPages;
    List<T> content;

    public static <T> PageDto<T> createDto(Page<T> page){
        return new PageDto<T>(
                page.isLast(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getContent()
        );
    }
}
