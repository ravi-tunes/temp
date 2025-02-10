    // DeserializationHandler: deserializes protoBytes into a Trade object.
    public static class DeserializationHandler implements EventHandler<TradeEvent>, LifeCycleAware {
        @Override
        public void onEvent(TradeEvent event, long sequence, boolean endOfBatch) throws Exception {
            if (event.getProtoBytes() != null && event.getTrade() == null) {
                Trade trade = Trade.parseFrom(event.getProtoBytes());
                event.setTrade(trade);
            }
        }
        @Override
        public void onStart() {
            System.out.println("DeserializationHandler started.");
        }
        @Override
        public void onShutdown() {
            System.out.println("DeserializationHandler shutting down.");
        }
    }