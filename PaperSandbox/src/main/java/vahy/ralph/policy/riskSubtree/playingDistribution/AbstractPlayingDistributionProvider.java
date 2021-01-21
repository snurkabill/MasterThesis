package vahy.ralph.policy.riskSubtree.playingDistribution;

import vahy.RiskState;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.ralph.metadata.RalphMetadata;

public abstract class AbstractPlayingDistributionProvider<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>
    implements PlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> {

    protected static final double TOLERANCE = Math.pow(10, -10);
    protected final boolean applyTemperature;

    protected AbstractPlayingDistributionProvider(boolean applyTemperature) {
        this.applyTemperature = applyTemperature;
    }
}
