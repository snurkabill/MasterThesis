package vahy.paperGenerics.selector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.nodeSelector.NodeSelector;
import vahy.paperGenerics.metadata.PaperMetadata;

public interface RiskAverseNodeSelector<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends State<TAction, TObservation, TState>> extends NodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> {

    void setAllowedRiskInRoot(double allowedRiskInRoot);

}
