package vahy.paperGenerics.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;

public interface PaperPolicy<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>>
    extends Policy<TAction, TObservation, TState, PaperPolicyRecord> {

    double[] getPriorActionProbabilityDistribution(TState gameState);

    double getEstimatedReward(TState gameState);

    double getEstimatedRisk(TState gameState);

    double getInnerRiskAllowed();
}
