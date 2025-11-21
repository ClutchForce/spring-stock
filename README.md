# Spring Stock Analysis

A multi-module Spring Boot 3 project for real-time stock analysis using Apache Kafka. This project demonstrates a complete streaming data pipeline from data generation through processing to real-time visualization.

## Architecture

The project consists of three independent Spring Boot applications:

```
┌─────────────────────┐      ┌──────────────────────┐      ┌─────────────────────┐
│  Market Simulator   │─────▶│  Stream Processor    │─────▶│ Dashboard Backend   │
│   (Producer)        │      │  (Kafka Streams)     │      │   (Consumer)        │
└─────────────────────┘      └──────────────────────┘      └─────────────────────┘
         │                              │                              │
         │                              │                              │
         ▼                              ▼                              ▼
  raw-stock-ticks              averaged-stock-prices            WebSocket Clients
     (Topic)                         (Topic)
```

## Modules

### 1. Market Simulator (Producer)
**Location:** `market-simulator/`

A Kafka producer that simulates real-time stock market data by generating random stock ticks for major tech stocks (AAPL, GOOGL, MSFT, AMZN, TSLA, META, NVDA, JPM).

**Features:**
- Sends JSON-formatted stock ticks every second
- Publishes to `raw-stock-ticks` Kafka topic
- Configurable for Confluent Cloud SASL/SSL authentication

**Run:**
```bash
cd market-simulator
mvn spring-boot:run
```

### 2. Stream Processor (Kafka Streams)
**Location:** `stream-processor/`

A Kafka Streams application that processes stock ticks in real-time using 5-minute tumbling windows to calculate average prices and total volumes.

**Features:**
- Consumes from `raw-stock-ticks` topic
- Calculates 5-minute tumbling window aggregations per stock symbol
- Outputs averaged prices to `averaged-stock-prices` topic
- Maintains running averages for price and total volume

**Run:**
```bash
cd stream-processor
mvn spring-boot:run
```

### 3. Dashboard Backend (Consumer + WebSocket)
**Location:** `dashboard-backend/`

A Spring Boot web application that consumes processed stock data and pushes it to connected WebSocket clients in real-time.

**Features:**
- Consumes from `averaged-stock-prices` topic
- Exposes WebSocket endpoint at `/ws`
- Broadcasts stock updates to `/topic/stock-prices`
- REST API ready for dashboard integration

**Run:**
```bash
cd dashboard-backend
mvn spring-boot:run
```

**WebSocket Connection:**
```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);
stompClient.connect({}, function() {
    stompClient.subscribe('/topic/stock-prices', function(message) {
        const stockData = JSON.parse(message.body);
        console.log(stockData);
    });
});
```

## Prerequisites

- Java 17 or higher
- Apache Maven 3.6+
- Apache Kafka 3.6+ (or Confluent Cloud account)

## Building the Project

Build all modules from the root directory:

```bash
mvn clean install
```

## Configuration

Each module includes an `application.yml` file with configuration templates for both local Kafka and Confluent Cloud.

### Local Kafka Setup

Default configuration uses `localhost:9092`:
```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

### Confluent Cloud Configuration

Uncomment and configure the following in each module's `application.yml`:

```yaml
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:pkc-xxxxx.region.provider.confluent.cloud:9092}
    properties:
      sasl.mechanism: PLAIN
      security.protocol: SASL_SSL
      sasl.jaas.config: org.apache.kafka.common.security.plain.PlainLoginModule required username="${KAFKA_API_KEY}" password="${KAFKA_API_SECRET}";
```

Set environment variables:
```bash
export KAFKA_BOOTSTRAP_SERVERS="pkc-xxxxx.region.provider.confluent.cloud:9092"
export KAFKA_API_KEY="your-api-key"
export KAFKA_API_SECRET="your-api-secret"
```

## Kafka Topics

The following topics need to be created before running the applications:

1. **raw-stock-ticks** - Raw stock tick data from the simulator
2. **averaged-stock-prices** - Aggregated 5-minute window averages

For local Kafka:
```bash
kafka-topics --create --topic raw-stock-ticks --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
kafka-topics --create --topic averaged-stock-prices --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

## Data Models

### StockTick (Raw Data)
```json
{
  "symbol": "AAPL",
  "price": 178.45,
  "volume": 5420,
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### AveragedStockPrice (Processed Data)
```json
{
  "symbol": "AAPL",
  "averagePrice": 178.23,
  "totalVolume": 162540,
  "count": 30,
  "windowStart": "2024-01-15T10:30:00Z",
  "windowEnd": "2024-01-15T10:35:00Z"
}
```

## Running the Complete Pipeline

1. Start Kafka (if running locally)
2. Create the required topics
3. Start the modules in order:
   ```bash
   # Terminal 1
   cd stream-processor && mvn spring-boot:run
   
   # Terminal 2
   cd dashboard-backend && mvn spring-boot:run
   
   # Terminal 3
   cd market-simulator && mvn spring-boot:run
   ```

4. Connect to the WebSocket endpoint to receive real-time updates

## Technology Stack

- **Spring Boot 3.2.0** - Application framework
- **Apache Kafka 3.6.0** - Message streaming platform
- **Kafka Streams** - Stream processing library
- **Spring Kafka** - Kafka integration
- **WebSocket (STOMP/SockJS)** - Real-time client communication
- **Lombok** - Boilerplate code reduction
- **Jackson** - JSON serialization

## License

This project is provided as-is for educational and demonstration purposes.
