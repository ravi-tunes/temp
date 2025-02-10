import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

// Extend with MockitoExtension to enable Mockito annotations if needed.
@ExtendWith(MockitoExtension.class)
public class AggregationKeyGeneratorTest {

    /**
     * A minimal Trade interface for testing purposes.
     * In your application, you likely already have a Trade class or interface.
     */
    interface Trade {
        String getBook();
        String getInstrument();
        String getCounterparty();
    }

    @Test
    public void testGenerateKeyBook() {
        // Create a Mockito mock for Trade.
        Trade trade = mock(Trade.class);
        when(trade.getBook()).thenReturn("BookA");
        // Even though these are set, they won't be used in the BOOK aggregation group.
        when(trade.getInstrument()).thenReturn("InstrumentX");
        when(trade.getCounterparty()).thenReturn("CounterpartyY");

        // For AggregationGroup.BOOK, only the book should be included.
        AggregationKey key = AggregationKeyGenerator.generateKey(trade, AggregationGroup.BOOK);
        assertEquals("BookA", key.getBook());
        assertNull(key.getInstrument(), "Instrument should be null for BOOK aggregation group");
        assertNull(key.getCounterparty(), "Counterparty should be null for BOOK aggregation group");
    }

    @Test
    public void testGenerateKeyBookInstrument() {
        Trade trade = mock(Trade.class);
        when(trade.getBook()).thenReturn("BookA");
        when(trade.getInstrument()).thenReturn("InstrumentX");
        when(trade.getCounterparty()).thenReturn("CounterpartyY");

        // For AggregationGroup.BOOK_INSTRUMENT, include book and instrument.
        AggregationKey key = AggregationKeyGenerator.generateKey(trade, AggregationGroup.BOOK_INSTRUMENT);
        assertEquals("BookA", key.getBook());
        assertEquals("InstrumentX", key.getInstrument());
        assertNull(key.getCounterparty(), "Counterparty should be null for BOOK_INSTRUMENT aggregation group");
    }

    @Test
    public void testGenerateKeyBookInstrumentCounterparty() {
        Trade trade = mock(Trade.class);
        when(trade.getBook()).thenReturn("BookA");
        when(trade.getInstrument()).thenReturn("InstrumentX");
        when(trade.getCounterparty()).thenReturn("CounterpartyY");

        // For AggregationGroup.BOOK_INSTRUMENT_COUNTERPARTY, all three fields should be included.
        AggregationKey key = AggregationKeyGenerator.generateKey(trade, AggregationGroup.BOOK_INSTRUMENT_COUNTERPARTY);
        assertEquals("BookA", key.getBook());
        assertEquals("InstrumentX", key.getInstrument());
        assertEquals("CounterpartyY", key.getCounterparty());
    }

    @Test
    public void testGenerateKeyWithNullTrade() {
        // Passing a null trade should throw a NullPointerException.
        Exception exception = assertThrows(NullPointerException.class, () -> {
            AggregationKeyGenerator.generateKey(null, AggregationGroup.BOOK);
        });
        assertNotNull(exception.getMessage(), "Exception message should not be null");
    }

    @Test
    public void testGenerateKeyWithNullGroup() {
        Trade trade = mock(Trade.class);
        when(trade.getBook()).thenReturn("BookA");
        when(trade.getInstrument()).thenReturn("InstrumentX");
        when(trade.getCounterparty()).thenReturn("CounterpartyY");

        // Passing a null aggregation group should result in a NullPointerException.
        Exception exception = assertThrows(NullPointerException.class, () -> {
            AggregationKeyGenerator.generateKey(trade, null);
        });
        assertNotNull(exception.getMessage(), "Exception message should not be null");
    }

    @Test
    public void testGenerateKeyConsistency() {
        // Two calls with the same trade and same aggregation group should produce equal keys.
        Trade trade = mock(Trade.class);
        when(trade.getBook()).thenReturn("BookB");
        when(trade.getInstrument()).thenReturn("InstrumentY");
        when(trade.getCounterparty()).thenReturn("CounterpartyZ");

        AggregationKey key1 = AggregationKeyGenerator.generateKey(trade, AggregationGroup.BOOK_INSTRUMENT_COUNTERPARTY);
        AggregationKey key2 = AggregationKeyGenerator.generateKey(trade, AggregationGroup.BOOK_INSTRUMENT_COUNTERPARTY);
        assertEquals(key1, key2, "Keys generated from identical inputs should be equal");
        assertEquals(key1.hashCode(), key2.hashCode(), "Hash codes should match for equal keys");
    }
}