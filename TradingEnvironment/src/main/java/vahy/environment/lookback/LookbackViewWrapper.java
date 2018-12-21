package vahy.environment.lookback;

public interface LookbackViewWrapper<T> extends ShiftableLookback {

    void initiateAt(int n);

    int getLastValidIndex();

}
