package vahy.paperGenerics.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;

public interface PaperPolicy<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends State<TAction, TObservation, TState>>
    extends Policy<TAction, TObservation, TState> {

    double[] getPriorActionProbabilityDistribution(StateWrapper<TAction, TObservation, TState> gameState);

    double getEstimatedReward(StateWrapper<TAction, TObservation, TState> gameState);

    double getEstimatedRisk(StateWrapper<TAction, TObservation, TState> gameState);

    double getInnerRiskAllowed();
}
