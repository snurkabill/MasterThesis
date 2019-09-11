package vahy.paperGenerics.reinforcement.episode.sampler;

import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicy;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.vizualiation.ProgressTrackerSettings;

public class EpisodeGameSampler<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractGameSampler<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private final PaperPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> playerPolicySupplier;

    public EpisodeGameSampler(InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                              PaperPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> playerPolicySupplier,
                              PaperPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> opponentPolicySupplier,
                              ProgressTrackerSettings progressTrackerSettings,
                              int stepCountLimit,
                              int processingUnitCount) {
        super(initialStateSupplier, opponentPolicySupplier, progressTrackerSettings, stepCountLimit, processingUnitCount);
        this.playerPolicySupplier = playerPolicySupplier;
    }

    @Override
    protected PaperPolicy<TAction, TPlayerObservation, TOpponentObservation, TState> supplyPlayerPolicy(TState initialState) { // TODO: delete this and PaperGameSampler and provide supplier to AbstractGameSampler
        return playerPolicySupplier.initializePolicy(initialState);
    }
}
