package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;

public abstract class AbstractPlayingDistributionProvider<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    implements PlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> {

    protected static final double TOLERANCE = Math.pow(10, -10);
    protected final boolean applyTemperature;

    protected AbstractPlayingDistributionProvider(boolean applyTemperature) {
        this.applyTemperature = applyTemperature;
    }
}
