package vahy.impl.model.reward;

import vahy.api.model.reward.DoubleVectorialReward;
import vahy.api.model.reward.Reward;

public class DoubleScalarRewardDouble implements DoubleVectorialReward {

    private final Double value;

    public DoubleScalarRewardDouble(Double value) {
        this.value = value;
    }

    public Double getValue() {
        return value;
    }

    @Override
    public int compareTo(Reward o) {
        if(this == o) {
            return 0;
        }
        if(!(o instanceof DoubleScalarRewardDouble)) {
            throw new IllegalArgumentException("Trying to compare non-comparable implementations. Base class: [" + DoubleScalarRewardDouble.class.getName() + "] comparing: [" + o + "]");
        }
        DoubleScalarRewardDouble other = (DoubleScalarRewardDouble) o;
        return this.value.compareTo(other.value);
    }

    @Override
    public int componentCount() {
        return 1;
    }

    @Override
    public String toPrettyString() {
        return value.toString();
    }

    @Override
    public double[] getAsVector() {
        return new double[] {value};
    }
}
