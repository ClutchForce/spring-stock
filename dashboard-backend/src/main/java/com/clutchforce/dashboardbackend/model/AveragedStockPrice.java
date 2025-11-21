package com.clutchforce.dashboardbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AveragedStockPrice {
    private String symbol;
    private double averagePrice;
    private long totalVolume;
    private long count;
    private Instant windowStart;
    private Instant windowEnd;
}
