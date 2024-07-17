package com.example.petbuddybackend.utils.paging;

import com.example.petbuddybackend.dto.paging.PagingParams;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;


public final class PagingUtils {

    private PagingUtils() {}

    public static Pageable createPageable(PagingParams params) {

        if(CollectionUtils.isEmpty(params.getSortBy())) {
            return PageRequest.of(
                params.getPage(),
                params.getSize()
            );
        }

        return PageRequest.of(
            params.getPage(),
            params.getSize(),
            Sort.by(
                params.getSortDirection(),
                params.getSortBy().toArray(String[]::new)
            )
        );
    }
}
