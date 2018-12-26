package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;

import java.util.List;

public interface Policy<
    TAction extends Action,
    TReward extends Reward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> {

    double[] getActionProbabilityDistribution(TState gameState);

    TAction getDiscreteAction(TState gameState);

    void updateStateOnPlayedActions(List<TAction> opponentActionList);
}
