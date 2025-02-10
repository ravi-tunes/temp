    public static class TradeEvent {
        private byte[] protoBytes;
        private Trade trade;

        public byte[] getProtoBytes() {
            return protoBytes;
        }
        public void setProtoBytes(byte[] protoBytes) {
            this.protoBytes = protoBytes;
        }
        public Trade getTrade() {
            return trade;
        }
        public void setTrade(Trade trade) {
            this.trade = trade;
        }
        public void clear() {
            protoBytes = null;
            trade = null;
        }
    }