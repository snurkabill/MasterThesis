package vahy.environment;

public enum TradingSystemState {
    NO_POSITION(false),
    LONG_POSITION(true),
    SHORT_POSITION(true);

    private final boolean isOpenPosition;

    TradingSystemState(boolean isOpenPosition) {
        this.isOpenPosition = isOpenPosition;
    }

    public boolean isOpenPosition() {
        return isOpenPosition;
    }

}
