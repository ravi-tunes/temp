import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AggregationKeyTest {

    @Test
    public void testGettersAndConstructor() {
        AggregationKey key = new AggregationKey("BookA", "InstrumentX", "CounterpartyY");
        assertEquals("BookA", key.getBook(), "Book getter should return 'BookA'");
        assertEquals("InstrumentX", key.getInstrument(), "Instrument getter should return 'InstrumentX'");
        assertEquals("CounterpartyY", key.getCounterparty(), "Counterparty getter should return 'CounterpartyY'");
    }

    @Test
    public void testEqualsAndHashCode() {
        AggregationKey key1 = new AggregationKey("BookA", "InstrumentX", "CounterpartyY");
        AggregationKey key2 = new AggregationKey("BookA", "InstrumentX", "CounterpartyY");
        AggregationKey key3 = new AggregationKey("BookA", "InstrumentX", null);

        // Keys with the same values should be equal and have the same hash code.
        assertEquals(key1, key2, "Keys with the same values must be equal");
        assertEquals(key1.hashCode(), key2.hashCode(), "Hash codes should match for equal keys");

        // Keys that differ should not be equal.
        assertNotEquals(key1, key3, "Keys that differ must not be equal");
    }

    @Test
    public void testToStringAllNonNull() {
        AggregationKey key = new AggregationKey("BookA", "InstrumentX", "CounterpartyY");
        String expected = "book=BookA;instrument=InstrumentX;counterparty=CounterpartyY";
        assertEquals(expected, key.toString(), "toString output must match expected format when all fields are non-null");
    }

    @Test
    public void testToStringWithNulls() {
        AggregationKey key = new AggregationKey("BookA", null, "CounterpartyY");
        String expected = "book=BookA;instrument=;counterparty=CounterpartyY";
        assertEquals(expected, key.toString(), "toString output must represent null fields as empty strings");
    }

    @Test
    public void testFromStringValid() {
        String keyStr = "book=BookA;instrument=InstrumentX;counterparty=CounterpartyY";
        AggregationKey key = AggregationKey.fromString(keyStr);
        assertEquals("BookA", key.getBook(), "Parsed book should be 'BookA'");
        assertEquals("InstrumentX", key.getInstrument(), "Parsed instrument should be 'InstrumentX'");
        assertEquals("CounterpartyY", key.getCounterparty(), "Parsed counterparty should be 'CounterpartyY'");
    }

    @Test
    public void testFromStringValidWithEmptyFields() {
        String keyStr = "book=BookA;instrument=;counterparty=";
        AggregationKey key = AggregationKey.fromString(keyStr);
        assertEquals("BookA", key.getBook(), "Parsed book should be 'BookA'");
        assertNull(key.getInstrument(), "Empty instrument should be parsed as null");
        assertNull(key.getCounterparty(), "Empty counterparty should be parsed as null");
    }

    @Test
    public void testFromStringEmptyInput() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            AggregationKey.fromString("");
        });
        assertTrue(exception.getMessage().contains("cannot be null or empty"),
                   "Empty string should trigger an IllegalArgumentException with appropriate message");
    }

    @Test
    public void testFromStringNullInput() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            AggregationKey.fromString(null);
        });
        assertTrue(exception.getMessage().contains("cannot be null or empty"),
                   "Null input should trigger an IllegalArgumentException with appropriate message");
    }

    @Test
    public void testFromStringMalformedPart() {
        // Example with a part that doesn't contain '='.
        String keyStr = "book=BookA;instrumentInstrumentX;counterparty=CounterpartyY";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            AggregationKey.fromString(keyStr);
        });
        assertTrue(exception.getMessage().contains("Invalid key-value format"),
                   "Malformed part should trigger an IllegalArgumentException");
    }

    @Test
    public void testFromStringUnexpectedKey() {
        // Example with an unexpected key.
        String keyStr = "book=BookA;instrument=InstrumentX;foo=bar";
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            AggregationKey.fromString(keyStr);
        });
        assertTrue(exception.getMessage().contains("Unexpected key"),
                   "Unexpected key should trigger an IllegalArgumentException");
    }

    @Test
    public void testToStringFromStringCycle() {
        AggregationKey original = new AggregationKey("BookA", "InstrumentX", "CounterpartyY");
        String keyStr = original.toString();
        AggregationKey parsed = AggregationKey.fromString(keyStr);
        assertEquals(original, parsed, "A key parsed from its toString representation should equal the original");
    }

    @Test
    public void testEqualsWithDifferentTypes() {
        AggregationKey key = new AggregationKey("BookA", "InstrumentX", "CounterpartyY");
        assertFalse(key.equals("some string"), "AggregationKey should not equal an object of a different type");
        assertFalse(key.equals(null), "AggregationKey should not equal null");
    }

    @Test
    public void testMockitoSpyUsage() {
        // Create a spy for AggregationKey.
        AggregationKey key = new AggregationKey("BookA", "InstrumentX", "CounterpartyY");
        AggregationKey spyKey = spy(key);
        
        // Invoke toString to trigger method call.
        String result = spyKey.toString();
        
        // Verify that toString was indeed called on the spy.
        verify(spyKey, times(1)).toString();
        assertNotNull(result, "The result of toString should not be null");
    }
}