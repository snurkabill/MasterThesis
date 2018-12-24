package vahy.paperGenerics.reinforcement.episode;

import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.paperGenerics.PaperState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.policy.PaperPolicy;
import vahy.paperGenerics.policy.PaperPolicySupplier;

public class EpisodeGameSampler<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TObservation extends DoubleVector,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TObservation, TState>>
    extends AbstractGameSampler<TAction, TReward, TObservation, TSearchNodeMetadata, TState> {

    private final PaperPolicySupplier<TAction, TReward, TObservation, TSearchNodeMetadata, TState> playerPolicySupplier;

    public EpisodeGameSampler(InitialStateSupplier<TAction, TReward, TObservation, TState> initialStateSupplier,
                              PaperPolicySupplier<TAction, TReward, TObservation, TSearchNodeMetadata, TState> playerPolicySupplier,
                              PaperPolicySupplier<TAction, TReward, TObservation, TSearchNodeMetadata, TState> opponentPolicySupplier,
                              int stepCountLimit) {
        super(initialStateSupplier, opponentPolicySupplier, stepCountLimit);
        this.playerPolicySupplier = playerPolicySupplier;
    }

    @Override
    protected PaperPolicy<TAction, TReward, TObservation, TState> supplyPlayerPolicy(TState initialState) {
        return playerPolicySupplier.initializePolicy(initialState);
    }
}
