package org.piet.forumbackend.globals.pagination;

import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;

@Value
public class PageDto<T> implements Serializable {
    boolean last;
    long totalElements;
    long totalPages;
    List<T> content;

    public static <T> PageDto<T> of(Page<T> page) {
        return new PageDto<T>(
                page.isLast(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getContent()
        );
    }

    public static <T> PageDto<T> fromPaged(List<T> list, Pageable pageable, long totalElements) {
        PageImpl<T> page = new PageImpl<T>(list, pageable, totalElements);
        return of(page);
    }

    public static <T> PageDto<T> fromPaged(List<T> list, PaginationDto pagination, long totalElements) {
        return fromPaged(list, pagination.toPageable(), totalElements);
    }

    public static <T> PageDto<T> fromUnpaged(List<T> list, Pageable pageable) {
        int offset = (int) pageable.getOffset();
        List<T> paged = offset >= list.size() ?
                List.of()
                :
                list.subList(offset, Math.min(offset + pageable.getPageSize(), list.size()));
        return fromPaged(paged, pageable, list.size());
    }

    public static <T> PageDto<T> fromUnpaged(List<T> list, PaginationDto pagination) {
        return fromUnpaged(list, pagination.toPageable());
    }
}
