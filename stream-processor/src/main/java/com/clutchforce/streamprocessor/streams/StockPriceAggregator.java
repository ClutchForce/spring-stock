package com.clutchforce.streamprocessor.streams;

import com.clutchforce.streamprocessor.model.AveragedStockPrice;
import com.clutchforce.streamprocessor.model.StockTick;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
@Slf4j
public class StockPriceAggregator {

    @Value("${kafka.topic.raw-stock-ticks}")
    private String inputTopic;

    @Value("${kafka.topic.averaged-stock-prices}")
    private String outputTopic;

    @Bean
    public KStream<String, AveragedStockPrice> kStream(StreamsBuilder streamsBuilder) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Configure JsonSerde with trusted packages
        JsonSerde<StockTick> stockTickSerde = new JsonSerde<>(StockTick.class, objectMapper);
        stockTickSerde.configure(Map.of(
            "spring.json.trusted.packages", "*",
            "spring.json.use.type.headers", "false"
        ), false);

        JsonSerde<AveragedStockPrice> averagedStockPriceSerde = new JsonSerde<>(AveragedStockPrice.class, objectMapper);
        averagedStockPriceSerde.configure(Map.of(
            "spring.json.trusted.packages", "*"
        ), false);
        KStream<String, StockTick> stockTickStream = streamsBuilder
            .stream(inputTopic, Consumed.with(Serdes.String(), stockTickSerde));

        // Group by symbol and create 5-minute tumbling windows
        KTable<Windowed<String>, AveragedStockPrice> averagedPrices = stockTickStream
            .groupByKey(Grouped.with(Serdes.String(), stockTickSerde))
            .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(5)))
            .aggregate(
                () -> new AveragedStockPrice(null, 0.0, 0L, 0L, null, null),
                (key, stockTick, aggregate) -> {
                    aggregate.setSymbol(key);
                    aggregate.setCount(aggregate.getCount() + 1);
                    aggregate.setTotalVolume(aggregate.getTotalVolume() + stockTick.getVolume());
                    
                    // Calculate running average
                    double newAverage = (aggregate.getAveragePrice() * (aggregate.getCount() - 1) + stockTick.getPrice()) 
                                      / aggregate.getCount();
                    aggregate.setAveragePrice(Math.round(newAverage * 100.0) / 100.0);
                    
                    return aggregate;
                },
                Materialized.with(Serdes.String(), averagedStockPriceSerde)
            );

        // Convert windowed KTable to KStream and set window boundaries
        KStream<String, AveragedStockPrice> outputStream = averagedPrices
            .toStream()
            .map((windowedKey, value) -> {
                value.setWindowStart(Instant.ofEpochMilli(windowedKey.window().start()));
                value.setWindowEnd(Instant.ofEpochMilli(windowedKey.window().end()));
                log.info("Window aggregate for {}: avg={}, volume={}, count={}, window=[{} - {}]",
                    windowedKey.key(), value.getAveragePrice(), value.getTotalVolume(), value.getCount(),
                    value.getWindowStart(), value.getWindowEnd());
                return KeyValue.pair(windowedKey.key(), value);
            });

        // Send aggregated results to output topic
        outputStream.to(outputTopic, Produced.with(Serdes.String(), averagedStockPriceSerde));

        return outputStream;
    }
}
