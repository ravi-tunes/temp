import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Main application class
public class TradeProcessingApplication {

    public static void main(String[] args) throws Exception {
        // Create an executor for the disruptors.
        ExecutorService executor = Executors.newCachedThreadPool();

        // --- Set up the secondary disruptor for publishing aggregation updates ---
        int updateBufferSize = 256;
        Disruptor<AggregationUpdateEvent> updateDisruptor =
                new Disruptor<>(AggregationUpdateEvent::new, updateBufferSize, executor, ProducerType.SINGLE, new BlockingWaitStrategy());
        AggregationUpdatePublishHandler updatePublishHandler = new AggregationUpdatePublishHandler();
        updateDisruptor.handleEventsWith(updatePublishHandler);
        updateDisruptor.start();

        // --- Set up the main disruptor for processing trade events ---
        int bufferSize = 1024;
        Disruptor<TradeEvent> disruptor =
                new Disruptor<>(TradeEvent::new, bufferSize, executor, ProducerType.SINGLE, new BlockingWaitStrategy());

        // Create event handlers.
        DeserializationHandler deserializationHandler = new DeserializationHandler();
        PersistEventHandler persistHandler = new PersistEventHandler("jdbc:postgresql://localhost:8812/qdb", "user", "password");
        // Create three aggregation event handlers for different aggregation groups.
        AggregationEventHandler handlerBook = new AggregationEventHandler(AggregationGroup.BOOK, updateDisruptor.getRingBuffer());
        AggregationEventHandler handlerBookInstrument = new AggregationEventHandler(AggregationGroup.BOOK_INSTRUMENT, updateDisruptor.getRingBuffer());
        AggregationEventHandler handlerBookInstrumentCounterparty = new AggregationEventHandler(AggregationGroup.BOOK_INSTRUMENT_COUNTERPARTY, updateDisruptor.getRingBuffer());
        AckHandler ackHandler = new AckHandler();

        // Set up the handler chain:
        // First, deserialization; then in parallel persist and aggregation handlers; then ack.
        disruptor.handleEventsWith(deserializationHandler)
                 .then(persistHandler, handlerBook, handlerBookInstrument, handlerBookInstrumentCounterparty)
                 .then(ackHandler);

        disruptor.start();

        // Simulate receiving Trade proto bytes from Solace.
        RingBuffer<TradeEvent> ringBuffer = disruptor.getRingBuffer();
        // For this simulation, publish 10 trade events.
        for (int i = 0; i < 10; i++) {
            long sequence = ringBuffer.next();
            try {
                TradeEvent event = ringBuffer.get(sequence);
                // Simulate proto bytes. In real use, these would come from Solace.
                event.setProtoBytes(("trade_proto_" + i).getBytes());
            } finally {
                ringBuffer.publish(sequence);
            }
        }

        // Allow some time for processing.
        Thread.sleep(2000);

        // Shutdown the disruptors and executor.
        disruptor.shutdown();
        updateDisruptor.shutdown();
        executor.shutdown();

        // Clean up resources in persist handler.
        persistHandler.shutdown();
    }