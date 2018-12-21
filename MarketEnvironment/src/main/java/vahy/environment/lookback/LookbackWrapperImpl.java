package vahy.environment.lookback;

import java.util.ArrayList;
import java.util.List;

public class LookbackWrapperImpl<T> implements LookbackViewWrapper {

    private final int lookbackLength;
    private final ArrayList<Double> sequence;
    private final int firstValidIndex = 0;
    private final int lastValidIndex;
    private int currentIndex = 0;

    public LookbackWrapperImpl(ArrayList<Double> sequence, int lookbackLength) {
        this.lookbackLength = lookbackLength;
        this.sequence = sequence;
        this.lastValidIndex = sequence.size() - lookbackLength;
    }

    @Override
    public void shiftBy(int n) {
        if(n + currentIndex > lastValidIndex) {
            throw new IllegalArgumentException("Last valid index for lb is: [" + lastValidIndex + "] but shifting from [" + currentIndex + "] to [" + n + currentIndex + "]");
        }
        currentIndex = currentIndex + n;
    }

    @Override
    public void initiateAt(int n) {
        if(n > lastValidIndex)  {
            throw new IllegalArgumentException("Last valid index for lb is: [" + lastValidIndex + "] but initiating at [" + n + "]");
        }
        if(n < firstValidIndex) {
            throw new IllegalArgumentException("First valid index for lb is: [" + firstValidIndex + "] but initiating at [" + n + "]");
        }
        currentIndex = n;
    }

    @Override
    public int getLastValidIndex() {
        return lastValidIndex;
    }

    @Override
    public boolean canBeShiftedBy(int n) {
        return n + currentIndex <= lastValidIndex;
    }

    @Override
    public List<Double> getLookback() {
        return sequence.subList(currentIndex, currentIndex + lookbackLength);
    }
}
