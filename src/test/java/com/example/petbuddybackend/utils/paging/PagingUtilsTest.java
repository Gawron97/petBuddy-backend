package com.example.petbuddybackend.utils.paging;

import com.example.petbuddybackend.dto.paging.PagingParams;
import com.example.petbuddybackend.dto.paging.SortedPagingParams;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class PagingUtilsTest {

    @ParameterizedTest
    @MethodSource("pagingParamsProvider")
    void testCreateSortedPageable_shouldReturnCorrectPageable(SortedPagingParams params) {
        Pageable pageable = PagingUtils.createSortedPageable(params);

        assertNotNull(pageable);
        assertEquals(params.getPage(), pageable.getPageNumber());
        assertEquals(params.getSize(), pageable.getPageSize());

        if (params.getSortBy() == null || params.getSortBy().isEmpty()) {
            assertFalse(pageable.getSort().isSorted());
        } else {
            Sort.Order order = pageable.getSort().getOrderFor(params.getSortBy().get(0));
            assertNotNull(order);
            assertEquals(params.getSortDirection(), order.getDirection());
        }
    }

    @Test
    void testCreatePageable_shouldCreateCorrectPageable() {
        PagingParams pagingParams = new PagingParams(1, 20);
        Pageable pageable = PagingUtils.createPageable(pagingParams);

        assertNotNull(pageable);
        assertEquals(1, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
        assertFalse(pageable.getSort().isSorted());
    }

    @Test
    void testSortedBy_shouldCreatePageableSortedByGivenProperty() {
        Pageable pageable = PageRequest.of(0, 10);
        Sort.Direction sortDirection = Sort.Direction.DESC;
        Pageable sortedPageable = PagingUtils.sortedBy(pageable, "propertyName", sortDirection);

        assertNotNull(sortedPageable);
        assertEquals(pageable.getPageNumber(), sortedPageable.getPageNumber());
        assertEquals(pageable.getPageSize(), sortedPageable.getPageSize());
        assertTrue(sortedPageable.getSort().isSorted());

        Sort.Order order = sortedPageable.getSort().getOrderFor("propertyName");
        assertNotNull(order);
        assertEquals(sortDirection, order.getDirection());
    }

    private static Stream<SortedPagingParams> pagingParamsProvider() {
        return Stream.of(
                new SortedPagingParams(0, 10, List.of("param1", "param2"), Sort.Direction.ASC),
                new SortedPagingParams(1, 20, null, null)
        );
    }
}
