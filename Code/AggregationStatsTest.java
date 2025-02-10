import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AggregationStatsTest {

    private AggregationStats stats;

    @BeforeEach
    public void setUp() {
        stats = new AggregationStats();
    }

    @Test
    public void testInitialState() {
        // Verify that a new AggregationStats instance has zero values and a non-null lastUpdated timestamp.
        assertEquals(0, stats.getTotalBuyQty(), "Initial totalBuyQty should be 0");
        assertEquals(BigDecimal.ZERO, stats.getTotalBuyNotional(), "Initial totalBuyNotional should be 0");
        assertEquals(BigDecimal.ZERO, stats.getAvgBuyPrice(), "Initial avgBuyPrice should be 0");

        assertEquals(0, stats.getTotalSellQty(), "Initial totalSellQty should be 0");
        assertEquals(BigDecimal.ZERO, stats.getTotalSellNotional(), "Initial totalSellNotional should be 0");
        assertEquals(BigDecimal.ZERO, stats.getAvgSellPrice(), "Initial avgSellPrice should be 0");

        assertEquals(0, stats.getNetQty(), "Initial netQty should be 0");
        assertEquals(BigDecimal.ZERO, stats.getNetNotional(), "Initial netNotional should be 0");
        assertEquals(BigDecimal.ZERO, stats.getNetAveragePrice(), "Initial netAveragePrice should be 0");

        assertNotNull(stats.getLastUpdated(), "Initial lastUpdated should not be null");
    }

    @Test
    public void testAddBuyTrade() {
        // Add a buy trade with qty=100 and price=10.
        stats.addBuyTrade(100, BigDecimal.valueOf(10));
        assertEquals(100, stats.getTotalBuyQty());
        assertEquals(BigDecimal.valueOf(1000).setScale(8, RoundingMode.HALF_UP), stats.getTotalBuyNotional());
        assertEquals(BigDecimal.valueOf(10).setScale(8, RoundingMode.HALF_UP), stats.getAvgBuyPrice());

        // Verify that timestamp is updated (non-null and recent)
        Instant ts = stats.getLastUpdated();
        assertNotNull(ts);
    }

    @Test
    public void testAddSellTrade() {
        // Add a sell trade with qty=80 and price=9.
        stats.addSellTrade(80, BigDecimal.valueOf(9));
        assertEquals(80, stats.getTotalSellQty());
        assertEquals(BigDecimal.valueOf(720).setScale(8, RoundingMode.HALF_UP), stats.getTotalSellNotional());
        assertEquals(BigDecimal.valueOf(9).setScale(8, RoundingMode.HALF_UP), stats.getAvgSellPrice());

        // Verify that timestamp is updated
        Instant ts = stats.getLastUpdated();
        assertNotNull(ts);
    }

    @Test
    public void testMultipleBuyTrades() {
        // Add multiple buy trades and verify the averages.
        stats.addBuyTrade(100, BigDecimal.valueOf(10)); // total: 1000
        stats.addBuyTrade(50, BigDecimal.valueOf(12));  // total: 600, new total qty = 150, total notional = 1600

        assertEquals(150, stats.getTotalBuyQty());
        assertEquals(BigDecimal.valueOf(1600).setScale(8, RoundingMode.HALF_UP), stats.getTotalBuyNotional());
        BigDecimal expectedAvgBuy = BigDecimal.valueOf(1600).divide(BigDecimal.valueOf(150), 8, RoundingMode.HALF_UP);
        assertEquals(expectedAvgBuy, stats.getAvgBuyPrice());
    }

    @Test
    public void testMultipleSellTrades() {
        // Add multiple sell trades.
        stats.addSellTrade(80, BigDecimal.valueOf(9));   // 720
        stats.addSellTrade(20, BigDecimal.valueOf(11));  // 220, new total qty = 100, total notional = 940

        assertEquals(100, stats.getTotalSellQty());
        assertEquals(BigDecimal.valueOf(940).setScale(8, RoundingMode.HALF_UP), stats.getTotalSellNotional());
        BigDecimal expectedAvgSell = BigDecimal.valueOf(940).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        assertEquals(expectedAvgSell, stats.getAvgSellPrice());
    }

    @Test
    public void testNetCalculations() {
        // Add buy trades.
        stats.addBuyTrade(150, BigDecimal.valueOf(10)); // total = 1500
        // Add sell trades.
        stats.addSellTrade(50, BigDecimal.valueOf(9));    // total = 450

        // Net qty = 150 - 50 = 100
        assertEquals(100, stats.getNetQty());
        // Net notional = 1500 - 450 = 1050
        assertEquals(BigDecimal.valueOf(1050).setScale(8, RoundingMode.HALF_UP), stats.getNetNotional());
        // Net avg price = 1050 / 100 = 10.5
        BigDecimal expectedNetAvg = BigDecimal.valueOf(1050).divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        assertEquals(expectedNetAvg, stats.getNetAveragePrice());
    }

    @Test
    public void testNetCalculationsWhenNoTrades() {
        // Without any trades, net values should be zero.
        assertEquals(0, stats.getNetQty());
        assertEquals(BigDecimal.ZERO, stats.getNetNotional());
        assertEquals(BigDecimal.ZERO, stats.getNetAveragePrice());
    }

    @Test
    public void testNetCalculationsWhenSellsExceedBuys() {
        // Add a buy trade.
        stats.addBuyTrade(100, BigDecimal.valueOf(10)); // total = 1000
        // Add a sell trade that exceeds the buy quantity.
        stats.addSellTrade(150, BigDecimal.valueOf(9));  // total = 1350

        // Net qty = 100 - 150 = -50
        assertEquals(-50, stats.getNetQty());
        // Net notional = 1000 - 1350 = -350
        assertEquals(BigDecimal.valueOf(-350).setScale(8, RoundingMode.HALF_UP), stats.getNetNotional());
        // Net avg price = (-350)/(-50) = 7.0
        BigDecimal expectedNetAvg = BigDecimal.valueOf(7).setScale(8, RoundingMode.HALF_UP);
        assertEquals(expectedNetAvg, stats.getNetAveragePrice());
    }

    @Test
    public void testZeroQuantityTrades() {
        // Add a buy trade with quantity 0. This should not change totals.
        stats.addBuyTrade(0, BigDecimal.valueOf(10));
        assertEquals(0, stats.getTotalBuyQty());
        assertEquals(BigDecimal.ZERO, stats.getTotalBuyNotional());
        assertEquals(BigDecimal.ZERO, stats.getAvgBuyPrice());

        // Add a sell trade with quantity 0.
        stats.addSellTrade(0, BigDecimal.valueOf(9));
        assertEquals(0, stats.getTotalSellQty());
        assertEquals(BigDecimal.ZERO, stats.getTotalSellNotional());
        assertEquals(BigDecimal.ZERO, stats.getAvgSellPrice());
    }

    @Test
    public void testTimestampUpdate() throws InterruptedException {
        // Capture timestamp before adding a trade.
        Instant before = stats.getLastUpdated();
        // Wait a bit to ensure a different timestamp.
        Thread.sleep(10);
        stats.addBuyTrade(100, BigDecimal.valueOf(10));
        Instant after = stats.getLastUpdated();
        assertTrue(after.isAfter(before), "Timestamp should be updated after trade addition");

        // Wait and then add a sell trade.
        Thread.sleep(10);
        Instant mid = stats.getLastUpdated();
        stats.addSellTrade(50, BigDecimal.valueOf(9));
        Instant later = stats.getLastUpdated();
        assertTrue(later.isAfter(mid), "Timestamp should be updated after sell trade");
    }

    @Test
    public void testToString() {
        // Add some trades.
        stats.addBuyTrade(100, BigDecimal.valueOf(10));
        stats.addSellTrade(50, BigDecimal.valueOf(9));
        String output = stats.toString();

        // Verify that the string output contains expected substrings.
        assertTrue(output.contains("totalBuyQty=" + stats.getTotalBuyQty()));
        assertTrue(output.contains("avgBuyPrice=" + stats.getAvgBuyPrice()));
        assertTrue(output.contains("totalSellQty=" + stats.getTotalSellQty()));
        assertTrue(output.contains("avgSellPrice=" + stats.getAvgSellPrice()));
        assertTrue(output.contains("netQty=" + stats.getNetQty()));
        assertTrue(output.contains("netNotional=" + stats.getNetNotional()));
        assertTrue(output.contains("netAveragePrice=" + stats.getNetAveragePrice()));
        assertTrue(output.contains("lastUpdated="));
    }

    @Test
    public void testMockitoSpyUsage() {
        // Use Mockito to create a spy of AggregationStats.
        AggregationStats spyStats = spy(new AggregationStats());
        // Invoke methods on the spy.
        spyStats.addBuyTrade(100, BigDecimal.valueOf(10));
        spyStats.addSellTrade(50, BigDecimal.valueOf(9));
        // Verify that the addBuyTrade and addSellTrade methods were called.
        verify(spyStats, times(1)).addBuyTrade(100, BigDecimal.valueOf(10));
        verify(spyStats, times(1)).addSellTrade(50, BigDecimal.valueOf(9));
    }
}