package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.policy.PlayingDistribution;

import java.util.EnumMap;

public class PlayingDistributionWithWithActionMap<TAction extends Enum<TAction> & Action> extends PlayingDistribution<TAction> {

    private final EnumMap<TAction, Double> actionMap;

    public PlayingDistributionWithWithActionMap(TAction action, double expectedReward, double[] distribution, EnumMap<TAction, Double> actionMap) {
        super(action, expectedReward, distribution);
        this.actionMap = actionMap;
    }

    public EnumMap<TAction, Double> getActionMap() {
        return actionMap;
    }
}
