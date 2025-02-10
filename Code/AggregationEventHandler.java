    // AggregationEventHandler: for a given AggregationGroup, update a HashMap of aggregation stats
    // and publish an aggregation update event.
    public static class AggregationEventHandler implements EventHandler<TradeEvent>, LifeCycleAware {
        private final AggregationGroup group;
        private final Map<AggregationKey, AggregationStats> aggregationMap = new HashMap<>();
        private final RingBuffer<AggregationUpdateEvent> updateRingBuffer;

        public AggregationEventHandler(AggregationGroup group, RingBuffer<AggregationUpdateEvent> updateRingBuffer) {
            this.group = group;
            this.updateRingBuffer = updateRingBuffer;
        }

        @Override
        public void onEvent(TradeEvent event, long sequence, boolean endOfBatch) throws Exception {
            Trade trade = event.getTrade();
            if (trade == null) return;
            // Generate the key based on the aggregation group.
            AggregationKey key = AggregationKeyGenerator.generateKey(trade, group);
            AggregationStats stats = aggregationMap.get(key);
            if (stats == null) {
                stats = new AggregationStats();
                aggregationMap.put(key, stats);
            }
            // Update stats based on trade side.
            if ("BUY".equalsIgnoreCase(trade.getSide())) {
                stats.addBuyTrade(trade.getQuantity(), trade.getPrice());
            } else if ("SELL".equalsIgnoreCase(trade.getSide())) {
                stats.addSellTrade(trade.getQuantity(), trade.getPrice());
            }
            // Publish the update to the secondary ring buffer.
            long updateSeq = updateRingBuffer.next();
            try {
                AggregationUpdateEvent updateEvent = updateRingBuffer.get(updateSeq);
                updateEvent.setKey(key);
                updateEvent.setStats(stats);
            } finally {
                updateRingBuffer.publish(updateSeq);
            }
        }

        @Override
        public void onStart() {
            System.out.println("AggregationEventHandler for " + group + " started.");
        }

        @Override
        public void onShutdown() {
            System.out.println("AggregationEventHandler for " + group + " shutting down.");
        }
    }