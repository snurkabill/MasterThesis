package vahy.paperGenerics.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.Reward;
import vahy.api.policy.Policy;

public interface PaperPolicy<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TState extends State<TAction, TReward, TObservation, TState>> extends Policy<TAction, TReward, TObservation, TState> {

    double[] getPriorActionProbabilityDistribution(TState gameState);

    TReward getEstimatedReward(TState gameState);

    double getEstimatedRisk(TState gameState);
}
