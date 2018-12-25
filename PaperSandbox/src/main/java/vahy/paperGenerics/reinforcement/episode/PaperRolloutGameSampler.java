package vahy.paperGenerics.reinforcement.episode;

import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicy;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.paperGenerics.policy.TrainablePaperPolicySupplier;

public class PaperRolloutGameSampler<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractGameSampler<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private final TrainablePaperPolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> playerPolicySupplier;

    public PaperRolloutGameSampler(InitialStateSupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                                   TrainablePaperPolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> playerPolicySupplier,
                                   PaperPolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> opponentPolicySupplier,
                                   int stepCountLimit) {
        super(initialStateSupplier, opponentPolicySupplier, stepCountLimit);
        this.playerPolicySupplier = playerPolicySupplier;

    }

    @Override
    protected PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> supplyPlayerPolicy(TState initialState) {
        return playerPolicySupplier.initializePolicyWithExploration(initialState);
    }
}
