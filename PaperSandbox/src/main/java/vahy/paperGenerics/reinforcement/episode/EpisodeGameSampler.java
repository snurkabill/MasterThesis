package vahy.paperGenerics.reinforcement.episode;

import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.paperGenerics.PaperState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.policy.PaperPolicy;
import vahy.paperGenerics.policy.PaperPolicySupplier;

public class EpisodeGameSampler<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractGameSampler<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private final PaperPolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> playerPolicySupplier;

    public EpisodeGameSampler(InitialStateSupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                              PaperPolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> playerPolicySupplier,
                              PaperPolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> opponentPolicySupplier,
                              int stepCountLimit) {
        super(initialStateSupplier, opponentPolicySupplier, stepCountLimit);
        this.playerPolicySupplier = playerPolicySupplier;
    }

    @Override
    protected PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> supplyPlayerPolicy(TState initialState) {
        return playerPolicySupplier.initializePolicy(initialState);
    }
}
