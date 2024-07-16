package com.example.petbuddybackend.utils.paging;

import com.example.petbuddybackend.dto.paging.PagingParams;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PagingUtils {

    private PagingUtils() {}

    public static Pageable createPageable(PagingParams params) {

        if(params.getSortBy().isBlank()) {
            return PageRequest.of(
                params.getPage(),
                params.getSize()
            );
        }

        return PageRequest.of(
            params.getPage(),
            params.getSize(),
            Sort.by(
                Sort.Direction.fromString(params.getSortDirection()),
                params.getSortBy()
            )
        );
    }
}
