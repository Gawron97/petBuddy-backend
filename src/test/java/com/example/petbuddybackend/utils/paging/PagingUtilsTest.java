package com.example.petbuddybackend.utils.paging;

import com.example.petbuddybackend.dto.paging.PagingParams;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class PagingUtilsTest {

    @ParameterizedTest
    @MethodSource("pagingParamsProvider")
    public void testCreatePageable(PagingParams params) {
        assertDoesNotThrow(() -> {
            PagingUtils.createPageable(params);
        });
    }

    private static Stream<PagingParams> pagingParamsProvider() {
        return Stream.of(
                new PagingParams(0, 10, List.of("par1", "par2"), Sort.Direction.ASC),
                new PagingParams(0, 10, null, null)
        );
    }
}
