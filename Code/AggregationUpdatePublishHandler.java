    public static class AggregationUpdatePublishHandler implements EventHandler<AggregationUpdateEvent>, LifeCycleAware {
        @Override
        public void onEvent(AggregationUpdateEvent event, long sequence, boolean endOfBatch) throws Exception {
            System.out.println("Publishing aggregation update: Key=" + event.getKey() + ", Stats=" + event.getStats());
            event.clear();
        }
        @Override
        public void onStart() {
            System.out.println("AggregationUpdatePublishHandler started.");
        }
        @Override
        public void onShutdown() {
            System.out.println("AggregationUpdatePublishHandler shutting down.");
        }
    }