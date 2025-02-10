    public static class AggregationUpdateEvent {
        private AggregationKey key;
        private AggregationStats stats;

        public AggregationKey getKey() {
            return key;
        }
        public void setKey(AggregationKey key) {
            this.key = key;
        }
        public AggregationStats getStats() {
            return stats;
        }
        public void setStats(AggregationStats stats) {
            this.stats = stats;
        }
        public void clear() {
            key = null;
            stats = null;
        }
    }