package com.clutchforce.marketsimulator.service;

import com.clutchforce.marketsimulator.client.FinnhubClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinnhubService {

    private final WebClient webClient;

    @Value("${finnhub.api-key}")
    private String apiKey;

    @Value("${finnhub.base-url:https://finnhub.io/api/v1}")
    private String baseUrl;

    /**
     * Fetches real-time stock quote from Finnhub API
     *
     * @param symbol Stock symbol (e.g., "AAPL")
     * @return FinnhubClient with price and volume data
     */
    public Mono<FinnhubClient> getStockQuote(String symbol) {
        return webClient
            .get()
            .uri(baseUrl + "/quote?symbol={symbol}&token={token}", symbol, apiKey)
            .retrieve()
            .bodyToMono(FinnhubClient.class)
            .doOnError(e -> log.error("Error fetching quote for {}: {}", symbol, e.getMessage()))
            .onErrorResume(e -> Mono.empty());
    }
}
