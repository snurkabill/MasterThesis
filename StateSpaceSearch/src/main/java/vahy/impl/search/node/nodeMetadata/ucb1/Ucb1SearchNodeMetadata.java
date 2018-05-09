package vahy.impl.search.node.nodeMetadata.ucb1;

import vahy.api.model.Action;
import vahy.api.model.reward.Reward;
import vahy.impl.search.node.nodeMetadata.AbstractSearchNodeMetadata;

import java.util.Map;

public class Ucb1SearchNodeMetadata<TAction extends Action, TReward extends Reward> extends AbstractSearchNodeMetadata<TAction, TReward, Ucb1StateActionMetadata<TReward>> {

    private int visitCounter = 1;

    public Ucb1SearchNodeMetadata(TReward cumulativeReward, Map<TAction, Ucb1StateActionMetadata<TReward>> tActionUcb1StateActionMetadataMap) {
        super(cumulativeReward, tActionUcb1StateActionMetadataMap);
    }

    public int getVisitCounter() {
        return visitCounter;
    }

    public void increaseVisitCounter() {
        visitCounter++;
    }
}
