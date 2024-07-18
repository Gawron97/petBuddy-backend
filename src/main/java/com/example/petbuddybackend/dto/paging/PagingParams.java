package com.example.petbuddybackend.dto.paging;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagingParams {

    @Min(0)
    private int page = 0;

    @Positive
    private int size = 10;

    @NotNull
    private List<String> sortBy = Collections.emptyList();

    @NotNull
    private Sort.Direction sortDirection = Sort.Direction.ASC;
}
