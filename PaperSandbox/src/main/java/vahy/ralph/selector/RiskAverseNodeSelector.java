package vahy.ralph.selector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.ralph.metadata.RalphMetadata;

public interface RiskAverseNodeSelector<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends State<TAction, TObservation, TState>> extends NodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> {

    void setAllowedRiskInRoot(double allowedRiskInRoot);

}
