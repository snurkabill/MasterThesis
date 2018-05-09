package vahy.impl.search.node.nodeMetadata.ucb1;

import vahy.api.model.reward.Reward;
import vahy.impl.search.node.nodeMetadata.AbstractStateActionMetadata;

public class Ucb1StateActionMetadata<TReward extends Reward> extends AbstractStateActionMetadata<TReward> {

    private int visitCounter = 1;

    public Ucb1StateActionMetadata(TReward gainedReward) {
        super(gainedReward);
    }

    public int getVisitCounter() {
        return visitCounter;
    }

    public void increaseVisitCounter() {
        visitCounter++;
    }
}
