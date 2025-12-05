package com.clutchforce.marketsimulator.client;

import lombok.Data;

@Data
public class FinnhubClient {
    private double c;      // Current price
    private long v;        // Volume
    private long t;        // Timestamp (Unix time)
}
