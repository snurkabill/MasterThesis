package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;

import java.util.List;

public interface Policy<TAction extends Action, TReward extends Reward, TObservation extends Observation> {

    double[] getActionProbabilityDistribution(State<TAction, TReward, TObservation> gameState);

    TAction getDiscreteAction(State<TAction, TReward, TObservation> gameState);

    void updateStateOnOpponentActions(List<TAction> opponentAction);
}
