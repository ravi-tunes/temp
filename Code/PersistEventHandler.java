import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.LifeCycleAware;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

public class PersistEventHandler implements EventHandler<TradeEvent>, LifeCycleAware {

    private final Connection connection;
    private final PreparedStatement preparedStatement;

    /**
     * Constructor creates a JDBC connection to QuestDB and prepares the SQL statement.
     *
     * @param jdbcUrl  The JDBC URL for QuestDB (e.g., "jdbc:postgresql://localhost:8812/qdb")
     * @param user     The username for QuestDB.
     * @param password The password for QuestDB.
     * @throws SQLException if there is an error establishing the connection or preparing the statement.
     */
    public PersistEventHandler(String jdbcUrl, String user, String password) throws SQLException {
        // Establish a connection to QuestDB.
        this.connection = DriverManager.getConnection(jdbcUrl, user, password);
        // Prepare the SQL insert statement.
        String sql = "INSERT INTO trades (trade_ts, folder, instrument, counterparty, book, side, price, quantity) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        this.preparedStatement = connection.prepareStatement(sql);
    }

    /**
     * Called by the Disruptor for each TradeEvent.
     *
     * @param event      The trade event.
     * @param sequence   The sequence number of the event in the ring buffer.
     * @param endOfBatch Indicates if this is the last event in the batch.
     */
    @Override
    public void onEvent(TradeEvent event, long sequence, boolean endOfBatch) throws Exception {
        persistTrade(event);
        // Clear the event for recycling.
        event.clear();
    }

    /**
     * Persists a single TradeEvent to QuestDB.
     *
     * @param event The trade event.
     * @throws SQLException if there is an error during the insert.
     */
    private void persistTrade(TradeEvent event) throws SQLException {
        // Set the timestamp (QuestDB accepts timestamps via JDBC).
        preparedStatement.setObject(1, event.getTradeTimestamp());
        // Set other fields.
        preparedStatement.setString(2, event.getFolder());
        preparedStatement.setString(3, event.getInstrument());
        preparedStatement.setString(4, event.getCounterparty());
        preparedStatement.setString(5, event.getBook());
        preparedStatement.setString(6, event.getSide()); // e.g., "BUY" or "SELL"
        preparedStatement.setBigDecimal(7, event.getPrice());
        preparedStatement.setLong(8, event.getQuantity());

        // Execute the insert.
        preparedStatement.executeUpdate();
    }

    /**
     * Called when the Disruptor starts.
     */
    @Override
    public void onStart() {
        // Optional initialization code can go here.
        System.out.println("PersistEventHandler started.");
    }

    /**
     * Called when the Disruptor is shutting down.
     * Here we tie the disruptor's shutdown event to our shutdown method.
     */
    @Override
    public void onShutdown() {
        System.out.println("PersistEventHandler shutting down.");
        shutdown();
    }

    /**
     * Closes JDBC resources.
     */
    public void shutdown() {
        try {
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
            System.out.println("JDBC resources closed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}