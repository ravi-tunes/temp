public enum AggregationGroup {
    BOOK,
    BOOK_INSTRUMENT,
    BOOK_INSTRUMENT_COUNTERPARTY
}

public final class AggregationKeyGenerator {

    /**
     * Generates an AggregationKey based on the provided trade and aggregation group.
     *
     * @param trade the trade object containing attributes (e.g., book, instrument, counterparty)
     * @param group the aggregation group to determine which fields to include
     * @return a new AggregationKey instance representing the key for the aggregation group
     * @throws IllegalArgumentException if an unsupported group is provided.
     */
    public static AggregationKey generateKey(Trade trade, AggregationGroup group) {
        switch (group) {
            case BOOK:
                return new AggregationKey(trade.getBook(), null, null);
            case BOOK_INSTRUMENT:
                return new AggregationKey(trade.getBook(), trade.getInstrument(), null);
            case BOOK_INSTRUMENT_COUNTERPARTY:
                return new AggregationKey(trade.getBook(), trade.getInstrument(), trade.getCounterparty());
            default:
                throw new IllegalArgumentException("Unsupported aggregation group: " + group);
        }
    }
}