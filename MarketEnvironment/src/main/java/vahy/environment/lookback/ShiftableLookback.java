package vahy.environment.lookback;

import java.util.List;

public interface ShiftableLookback {

    void shiftBy(int n);

    boolean canBeShiftedBy(int n);

    List<Double> getLookback();
}
