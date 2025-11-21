package com.clutchforce.dashboardbackend.consumer;

import com.clutchforce.dashboardbackend.model.AveragedStockPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AveragedStockPriceConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "${kafka.topic.averaged-stock-prices}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(AveragedStockPrice averagedStockPrice) {
        log.info("Consumed averaged stock price: {}", averagedStockPrice);
        
        // Push to WebSocket clients subscribed to /topic/stock-prices
        messagingTemplate.convertAndSend("/topic/stock-prices", averagedStockPrice);
        
        log.debug("Sent averaged stock price to WebSocket clients: {}", averagedStockPrice);
    }
}
