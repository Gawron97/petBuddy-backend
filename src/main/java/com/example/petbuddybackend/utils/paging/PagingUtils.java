package com.example.petbuddybackend.utils.paging;

import com.example.petbuddybackend.dto.paging.PagingParams;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.CollectionUtils;

import java.util.List;

public final class PagingUtils {

    private PagingUtils() {
    }

    public static Pageable createPageable(PagingParams params, Sort.NullHandling nullHandlingStrategy) {

        if(CollectionUtils.isEmpty(params.getSortBy())) {
            return PageRequest.of(
                    params.getPage(),
                    params.getSize()
            );
        }

        List<Sort.Order> orders = params.getSortBy().stream()
                .map(field -> new Sort.Order(
                        params.getSortDirection(),
                        field,
                        nullHandlingStrategy
                ))
                .toList();

        Sort sort = Sort.by(orders);

        return PageRequest.of(
                params.getPage(),
                params.getSize(),
                sort
        );
    }

    public static Pageable createPageable(PagingParams params) {
        return createPageable(params, Sort.NullHandling.NULLS_LAST);
    }
}
