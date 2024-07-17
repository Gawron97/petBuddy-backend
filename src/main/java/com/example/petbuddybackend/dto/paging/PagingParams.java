package com.example.petbuddybackend.dto.paging;

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
    private int page = 0;
    private int size = 10;
    private List<String> sortBy = Collections.emptyList();
    private Sort.Direction sortDirection = Sort.Direction.ASC;
}
