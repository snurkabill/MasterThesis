package vahy.impl.model.reward;

import vahy.api.model.reward.Reward;

public class DoubleScalarReward implements Reward {

    private final Double value;

    public DoubleScalarReward(Double value) {
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
        if(!(o instanceof DoubleScalarReward)) {
            throw new IllegalArgumentException("Trying to compare non-comparable implementations. Base class: [" + DoubleScalarReward.class.getName() + "] comparing: [" + o + "]");
        }
        DoubleScalarReward other = (DoubleScalarReward) o;
        return this.value.compareTo(other.value);
    }

    @Override
    public String toPrettyString() {
        return value.toString();
    }

}
