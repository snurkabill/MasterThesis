package vahy.paperGenerics;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.policy.Policy;
import vahy.impl.model.reward.DoubleReward;

public interface PaperPolicy<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TState extends State<TAction, TReward, TObservation, TState>> extends Policy<TAction, TReward, TObservation, TState> {

    double[] getPriorActionProbabilityDistribution(TState gameState);

    DoubleReward getEstimatedReward(TState gameState);

    double getEstimatedRisk(TState gameState);
}
