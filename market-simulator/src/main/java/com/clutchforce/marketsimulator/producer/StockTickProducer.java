package com.clutchforce.marketsimulator.producer;

import com.clutchforce.marketsimulator.model.StockTick;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockTickProducer {

    private final KafkaTemplate<String, StockTick> kafkaTemplate;

    @Value("${kafka.topic.raw-stock-ticks}")
    private String topic;

    private static final String[] SYMBOLS = {"AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "META", "NVDA", "JPM"};
    private static final double BASE_PRICE = 100.0;
    private static final Random random = new Random();

    @Scheduled(fixedRate = 1000) // Send stock ticks every second
    public void sendStockTick() {
        String symbol = SYMBOLS[random.nextInt(SYMBOLS.length)];
        double price = BASE_PRICE + (random.nextDouble() * 100);
        long volume = ThreadLocalRandom.current().nextLong(100, 10000);
        
        StockTick stockTick = new StockTick(
            symbol,
            Math.round(price * 100.0) / 100.0, // Round to 2 decimal places
            volume,
            Instant.now()
        );

        kafkaTemplate.send(topic, symbol, stockTick)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("Sent stock tick: {}", stockTick);
                } else {
                    log.error("Failed to send stock tick: {}", stockTick, ex);
                }
            });
    }
}
