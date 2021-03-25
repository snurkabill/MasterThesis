package vahy.ralph.policy.linearProgram;

import vahy.RiskState;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.ralph.metadata.RalphMetadata;

public class InnerElement<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>  {

    protected final SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node;
    protected final double modifier;
    protected FlowWithCoefficient flowWithCoefficient;

    public InnerElement(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node, double modifier, FlowWithCoefficient flowWithCoefficient) {
        this.node = node;
        this.modifier = modifier;
        this.flowWithCoefficient = flowWithCoefficient;
    }

    public SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> getNode() {
        return node;
    }

    public double getModifier() {
        return modifier;
    }

    public FlowWithCoefficient getFlowWithCoefficient() {
        return flowWithCoefficient;
    }
}
