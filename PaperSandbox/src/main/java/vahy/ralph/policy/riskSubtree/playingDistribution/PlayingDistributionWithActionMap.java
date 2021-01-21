package vahy.ralph.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.policy.PlayingDistribution;

import java.util.EnumMap;

public class PlayingDistributionWithActionMap<TAction extends Enum<TAction> & Action> extends PlayingDistribution<TAction> {

    private final EnumMap<TAction, Double> actionMap;

    public PlayingDistributionWithActionMap(TAction action, double expectedReward, double[] distribution, EnumMap<TAction, Double> actionMap) {
        super(action, expectedReward, distribution);
        this.actionMap = actionMap;
    }

    public EnumMap<TAction, Double> getActionMap() {
        return actionMap;
    }
}
