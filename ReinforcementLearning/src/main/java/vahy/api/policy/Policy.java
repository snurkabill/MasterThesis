package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;

import java.util.List;

public interface Policy<TAction extends Action, TReward extends Reward, TObservation extends Observation, TState extends State<TAction, TReward, TObservation, TState>> {

    double[] getActionProbabilityDistribution(TState gameState);

    TAction getDiscreteAction(TState gameState);

    void updateStateOnOpponentActions(List<TAction> opponentActionList);
}
