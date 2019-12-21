package vahy.paperGenerics.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;

public interface PaperPolicy<
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends Policy<TAction, TPlayerObservation, TOpponentObservation, TState, PaperPolicyRecord> {

    double[] getPriorActionProbabilityDistribution(TState gameState);

    double getEstimatedReward(TState gameState);

    double getEstimatedRisk(TState gameState);

    double getInnerRiskAllowed();
}
