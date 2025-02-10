import java.util.Objects;

public final class AggregationKey {
    private final String book;
    private final String instrument;
    private final String counterparty;
    private final int hashCode;

    /**
     * Constructs an AggregationKey with the given fields.
     * 
     * @param book         the book value (can be null)
     * @param instrument   the instrument value (can be null)
     * @param counterparty the counterparty value (can be null)
     */
    public AggregationKey(String book, String instrument, String counterparty) {
        this.book = book;
        this.instrument = instrument;
        this.counterparty = counterparty;
        this.hashCode = Objects.hash(book, instrument, counterparty);
    }

    public String getBook() {
        return book;
    }

    public String getInstrument() {
        return instrument;
    }

    public String getCounterparty() {
        return counterparty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AggregationKey)) return false;
        AggregationKey that = (AggregationKey) o;
        return Objects.equals(book, that.book) &&
               Objects.equals(instrument, that.instrument) &&
               Objects.equals(counterparty, that.counterparty);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Returns a string representation of the AggregationKey in a reproducible format.
     * The format is:
     *
     *     book=<book_value>;instrument=<instrument_value>;counterparty=<counterparty_value>
     *
     * If a field is null, it is represented as an empty string.
     */
    @Override
    public String toString() {
        return "book=" + (book != null ? book : "") +
               ";instrument=" + (instrument != null ? instrument : "") +
               ";counterparty=" + (counterparty != null ? counterparty : "");
    }

    /**
     * Parses an AggregationKey from its string representation.
     *
     * The expected format is:
     *
     *     book=<book_value>;instrument=<instrument_value>;counterparty=<counterparty_value>
     *
     * For example: "book=BookA;instrument=InstrumentX;counterparty=CounterpartyY"
     * An empty value is interpreted as null.
     *
     * @param s the string representation
     * @return a new AggregationKey instance
     * @throws IllegalArgumentException if the input is null, empty, or improperly formatted.
     */
    public static AggregationKey fromString(String s) {
        if (s == null || s.isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be null or empty.");
        }
        String[] parts = s.split(";");
        String book = null;
        String instrument = null;
        String counterparty = null;
        for (String part : parts) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length != 2) {
                throw new IllegalArgumentException("Invalid key-value format in part: " + part);
            }
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            if (value.isEmpty()) {
                value = null;
            }
            switch (key) {
                case "book":
                    book = value;
                    break;
                case "instrument":
                    instrument = value;
                    break;
                case "counterparty":
                    counterparty = value;
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected key: " + key);
            }
        }
        return new AggregationKey(book, instrument, counterparty);
    }
}