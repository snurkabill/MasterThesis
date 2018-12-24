package vahy.paperGenerics.reinforcement.episode;

import vahy.api.model.Action;
import vahy.paperGenerics.PaperState;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.policy.PaperPolicy;

public class EpisodeImmutableSetup<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TObservation extends DoubleVector,
    TState extends PaperState<TAction, TReward, TObservation, TState>>  {

    private final TState initialState;
    private final PaperPolicy<TAction, TReward, TObservation, TState> playerPaperPolicy;
    private final PaperPolicy<TAction, TReward, TObservation, TState> opponentPolicy;
    private final int stepCountLimit;

    public EpisodeImmutableSetup(TState initialState,
                                 PaperPolicy<TAction, TReward, TObservation, TState> playerPaperPolicy,
                                 PaperPolicy<TAction, TReward, TObservation, TState> opponentPolicy,
                                 int stepCountLimit) {
        this.initialState = initialState;
        this.playerPaperPolicy = playerPaperPolicy;
        this.opponentPolicy = opponentPolicy;
        this.stepCountLimit = stepCountLimit;
    }

    public TState getInitialState() {
        return initialState;
    }

    public PaperPolicy<TAction, TReward, TObservation, TState> getPlayerPaperPolicy() {
        return playerPaperPolicy;
    }

    public PaperPolicy<TAction, TReward, TObservation, TState> getOpponentPolicy() {
        return opponentPolicy;
    }

    public int getStepCountLimit() {
        return stepCountLimit;
    }
}
