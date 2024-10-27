package com.example.petbuddybackend.utils.paging;

import com.example.petbuddybackend.dto.paging.PagingParams;
import com.example.petbuddybackend.dto.paging.SortedPagingParams;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;


public final class PagingUtils {

    private PagingUtils() {}

    public static Pageable createSortedPageable(SortedPagingParams params) {

        if(CollectionUtils.isEmpty(params.getSortBy())) {
            return createPageable(params);
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

    public static Pageable createPageable(PagingParams pagingParams) {
        return PageRequest.of(
                pagingParams.getPage(),
                pagingParams.getSize()
        );
    }

    public static Pageable sortedBy(Pageable pageable, String propertyName) {
        Sort sort = Sort.by(Sort.Order.asc(propertyName));
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
