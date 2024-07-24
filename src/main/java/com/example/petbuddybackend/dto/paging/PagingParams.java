package com.example.petbuddybackend.dto.paging;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagingParams {

    @Min(0)
    @Schema(defaultValue = "0", description = "Page number")
    private int page = 0;

    @Positive
    @Schema(defaultValue = "10", description = "Page size")
    private int size = 10;

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
}
