package vahy.ralph.policy.riskSubtree;

import vahy.RiskState;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.ralph.metadata.RalphMetadata;

public interface SubtreeRiskCalculator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>  {

    double calculateRisk(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> subtreeRoot);

    String toLog();

}
