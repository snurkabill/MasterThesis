package vahy.impl.search.node.nodeMetadata;

import vahy.api.model.Action;
import vahy.api.model.reward.Reward;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;

import java.util.Map;

public class StateValueMetadataImpl<TAction extends Action, TReward extends Reward, TValue extends Comparable<TValue>, TStateActionMetadata extends StateActionMetadata<TReward>>
    extends AbstractSearchNodeMetadata<TAction, TReward, TStateActionMetadata>
    implements Comparable<StateValueMetadataImpl<TAction, TReward, TValue, TStateActionMetadata>> {

    private TValue stateValue;

    public StateValueMetadataImpl(TValue stateValue, TReward cumulativeReward, Map<TAction, TStateActionMetadata> stateActionMetadataMap) {
        super(cumulativeReward, stateActionMetadataMap);
        this.stateValue = stateValue;
    }

    public TValue getStateValue() {
        return stateValue;
    }

    public void setStateValue(TValue stateValue) {
        this.stateValue = stateValue;
    }

    @Override
    public int compareTo(StateValueMetadataImpl<TAction, TReward, TValue, TStateActionMetadata> o) {
        if(this == o) {
            return 0;
        }
        return stateValue.compareTo(o.stateValue);
    }

}
