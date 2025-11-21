package com.clutchforce.streamprocessor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockTick {
    private String symbol;
    private double price;
    private long volume;
    private Instant timestamp;
}
