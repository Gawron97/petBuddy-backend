package com.example.petbuddybackend.dto.paging;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
