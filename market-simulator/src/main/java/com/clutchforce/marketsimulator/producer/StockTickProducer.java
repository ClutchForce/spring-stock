package com.clutchforce.marketsimulator.producer;

import com.clutchforce.marketsimulator.model.StockTick;
import com.clutchforce.marketsimulator.service.FinnhubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockTickProducer {

    private final KafkaTemplate<String, StockTick> kafkaTemplate;
    private final FinnhubService finnhubService;

    @Value("${kafka.topic.raw-stock-ticks}")
    private String topic;

    // List of real stock symbols to track
    private static final List<String> SYMBOLS = List.of(
        "AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "META", "NVDA", "JPM"
    );
    
    private final AtomicInteger symbolIndex = new AtomicInteger(0);

    /**
     * Scheduled method to fetch and send real stock quotes from Finnhub every 5 seconds
     * Note: Finnhub free tier allows 60 calls/minute, so we space calls appropriately
     */
    @Scheduled(fixedRate = 5000) // Fetch every 5 seconds to stay within rate limits
    public void sendStockTick() {
        String symbol = SYMBOLS.get(symbolIndex.getAndUpdate(i -> (i + 1) % SYMBOLS.size()));
        
        finnhubService.getStockQuote(symbol)
            .subscribe(
                finnhubData -> {
                    StockTick stockTick = new StockTick(
                        symbol,
                        finnhubData.getC(),           // Current price from Finnhub
                        finnhubData.getV(),           // Volume from Finnhub
                        Instant.ofEpochSecond(finnhubData.getT()) // Convert Unix timestamp to Instant
                    );

                    kafkaTemplate.send(topic, symbol, stockTick)
                        .whenComplete((result, ex) -> {
                            if (ex == null) {
                                log.info("Sent stock tick for {}: Price=${}, Volume={}", 
                                    symbol, stockTick.getPrice(), stockTick.getVolume());
                            } else {
                                log.error("Failed to send stock tick for {}: {}", symbol, ex.getMessage());
                            }
                        });
                },
                error -> log.error("Error fetching quote for {}: {}", symbol, error.getMessage())
            );
    }
}
