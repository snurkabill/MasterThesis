package vahy.impl.model.reward;

import vahy.api.model.reward.DoubleVectorialReward;
import vahy.api.model.reward.Reward;

public class DoubleReward implements DoubleVectorialReward {

    private final Double value;

    public DoubleReward(Double value) {
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
        if(!(o instanceof DoubleReward)) {
            throw new IllegalArgumentException("Trying to compare non-comparable implementations. Base class: [" + DoubleReward.class.getName() + "] comparing: [" + o + "]");
        }
        DoubleReward other = (DoubleReward) o;
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
