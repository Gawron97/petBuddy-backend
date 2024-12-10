package com.example.petbuddybackend.dto.statistic;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

public record MonthlyRevenueDTO(
        Map<YearMonth, BigDecimal> monthlyRevenue
) {}
