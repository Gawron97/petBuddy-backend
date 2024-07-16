package com.example.petbuddybackend.dto.paging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagingParams {
    private int page = 0;
    private int size = 10;
    private String sortBy = "";
    private String sortDirection = "asc";
}
