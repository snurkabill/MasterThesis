package vahy.paperGenerics.selector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNodeMetadata;
import vahy.api.search.nodeSelector.NodeSelector;

public interface RiskAverseNodeSelector<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends SearchNodeMetadata,
    TState extends State<TAction, TObservation, TState>> extends NodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> {

    void setAllowedRiskInRoot(double allowedRiskInRoot);

}
