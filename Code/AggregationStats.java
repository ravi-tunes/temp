import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

public class AggregationStats {

    // Buy-side fields
    private long totalBuyQty;
    private BigDecimal totalBuyNotional = BigDecimal.ZERO;  // Sum of (buyQty * buyPrice)
    private BigDecimal avgBuyPrice = BigDecimal.ZERO;        // totalBuyNotional / totalBuyQty

    // Sell-side fields
    private long totalSellQty;
    private BigDecimal totalSellNotional = BigDecimal.ZERO;  // Sum of (sellQty * sellPrice)
    private BigDecimal avgSellPrice = BigDecimal.ZERO;         // totalSellNotional / totalSellQty

    // Timestamp for the last update (either buy or sell)
    private Instant lastUpdated;

    // Constructor initializes lastUpdated to the current time.
    public AggregationStats() {
        lastUpdated = Instant.now();
    }

    /**
     * Adds a BUY trade and updates the aggregated buy-side statistics.
     * 
     * @param qty   The quantity of the buy trade (must be positive).
     * @param price The trade price as a BigDecimal.
     */
    public void addBuyTrade(long qty, BigDecimal price) {
        // Calculate trade notional: price * quantity
        BigDecimal tradeNotional = price.multiply(BigDecimal.valueOf(qty));
        totalBuyNotional = totalBuyNotional.add(tradeNotional);
        totalBuyQty += qty;

        // Update average buy price if quantity > 0
        if (totalBuyQty > 0) {
            avgBuyPrice = totalBuyNotional.divide(BigDecimal.valueOf(totalBuyQty), 8, RoundingMode.HALF_UP);
        } else {
            avgBuyPrice = BigDecimal.ZERO;
        }

        // Update the timestamp to the current time.
        lastUpdated = Instant.now();
    }

    /**
     * Adds a SELL trade and updates the aggregated sell-side statistics.
     * 
     * @param qty   The quantity of the sell trade (must be positive).
     * @param price The trade price as a BigDecimal.
     */
    public void addSellTrade(long qty, BigDecimal price) {
        // Calculate trade notional: price * quantity
        BigDecimal tradeNotional = price.multiply(BigDecimal.valueOf(qty));
        totalSellNotional = totalSellNotional.add(tradeNotional);
        totalSellQty += qty;

        // Update average sell price if quantity > 0
        if (totalSellQty > 0) {
            avgSellPrice = totalSellNotional.divide(BigDecimal.valueOf(totalSellQty), 8, RoundingMode.HALF_UP);
        } else {
            avgSellPrice = BigDecimal.ZERO;
        }

        // Update the timestamp to the current time.
        lastUpdated = Instant.now();
    }

    /**
     * Returns the net quantity: (totalBuyQty - totalSellQty).
     *
     * @return Net quantity as a long.
     */
    public long getNetQty() {
        return totalBuyQty - totalSellQty;
    }

    /**
     * Returns the net notional: (totalBuyNotional - totalSellNotional).
     *
     * @return Net notional as a BigDecimal.
     */
    public BigDecimal getNetNotional() {
        return totalBuyNotional.subtract(totalSellNotional);
    }

    /**
     * Returns the net average price, defined as (net notional / net quantity).
     * If net quantity is zero, returns BigDecimal.ZERO.
     * Note: If sells exceed buys, the result will be negative.
     *
     * @return Net average price as a BigDecimal.
     */
    public BigDecimal getNetAveragePrice() {
        long netQty = getNetQty();
        if (netQty != 0) {
            return getNetNotional().divide(BigDecimal.valueOf(netQty), 8, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.ZERO;
        }
    }

    // --- Getters for Buy-side ---
    public long getTotalBuyQty() {
        return totalBuyQty;
    }

    public BigDecimal getTotalBuyNotional() {
        return totalBuyNotional;
    }

    public BigDecimal getAvgBuyPrice() {
        return avgBuyPrice;
    }

    // --- Getters for Sell-side ---
    public long getTotalSellQty() {
        return totalSellQty;
    }

    public BigDecimal getTotalSellNotional() {
        return totalSellNotional;
    }

    public BigDecimal getAvgSellPrice() {
        return avgSellPrice;
    }

    /**
     * Returns the timestamp of the last update (buy or sell trade).
     *
     * @return Last updated Instant.
     */
    public Instant getLastUpdated() {
        return lastUpdated;
    }

    @Override
    public String toString() {
        return "AggregationStats{" +
               "totalBuyQty=" + totalBuyQty +
               ", totalBuyNotional=" + totalBuyNotional +
               ", avgBuyPrice=" + avgBuyPrice +
               ", totalSellQty=" + totalSellQty +
               ", totalSellNotional=" + totalSellNotional +
               ", avgSellPrice=" + avgSellPrice +
               ", netQty=" + getNetQty() +
               ", netNotional=" + getNetNotional() +
               ", netAveragePrice=" + getNetAveragePrice() +
               ", lastUpdated=" + lastUpdated +
               '}';
    }
}