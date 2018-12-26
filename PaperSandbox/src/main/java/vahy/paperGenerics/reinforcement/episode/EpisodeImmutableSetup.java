package vahy.paperGenerics.reinforcement.episode;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicy;

public class EpisodeImmutableSetup<
    TAction extends Enum<TAction> & Action,
    TReward extends DoubleReward,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>  {

    private final TState initialState;
    private final PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> playerPaperPolicy;
    private final PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> opponentPolicy;
    private final int stepCountLimit;

    public EpisodeImmutableSetup(TState initialState,
                                 PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> playerPaperPolicy,
                                 PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> opponentPolicy,
                                 int stepCountLimit) {
        this.initialState = initialState;
        this.playerPaperPolicy = playerPaperPolicy;
        this.opponentPolicy = opponentPolicy;
        this.stepCountLimit = stepCountLimit;
    }

    public TState getInitialState() {
        return initialState;
    }

    public PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> getPlayerPaperPolicy() {
        return playerPaperPolicy;
    }

    public PaperPolicy<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> getOpponentPolicy() {
        return opponentPolicy;
    }

    public int getStepCountLimit() {
        return stepCountLimit;
    }
}
