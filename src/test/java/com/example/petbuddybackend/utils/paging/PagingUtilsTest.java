package com.example.petbuddybackend.utils.paging;

import com.example.petbuddybackend.dto.paging.SortedPagingParams;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class PagingUtilsTest {

    @ParameterizedTest
    @MethodSource("pagingParamsProvider")
    public void testCreatePageable(SortedPagingParams params) {
        assertDoesNotThrow(() -> {
            PagingUtils.createSortedPageable(params);
        });
    }

    private static Stream<SortedPagingParams> pagingParamsProvider() {
        return Stream.of(
                new SortedPagingParams(0, 10, List.of("par1", "par2"), Sort.Direction.ASC),
                new SortedPagingParams(0, 10, null, null)
        );
    }
}
