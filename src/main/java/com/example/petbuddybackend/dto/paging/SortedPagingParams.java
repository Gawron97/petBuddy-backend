package com.example.petbuddybackend.dto.paging;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SortedPagingParams extends PagingParams {

    @Schema(
            description = "Sort by field name from dto. Fields can be separated by coma. The order of fields is important.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            type = "array"
    )
    private List<String> sortBy;

    @Schema(
            description = "Sort direction",
            allowableValues = {"ASC", "DESC"},
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Sort.Direction sortDirection = Sort.Direction.ASC;

    public SortedPagingParams(int page, int size, List<String> sortBy, Sort.Direction sortDirection) {
        super(page, size);
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
    }
}
