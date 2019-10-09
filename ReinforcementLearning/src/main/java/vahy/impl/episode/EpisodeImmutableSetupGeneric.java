package vahy.impl.episode;

import vahy.api.episode.EpisodeImmutableSetup;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyRecord;

public class EpisodeImmutableSetupGeneric<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord>
    implements EpisodeImmutableSetup<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> {

    private final TState initialState;
    private final Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> playerPaperPolicy;
    private final Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicy;
    private final int stepCountLimit;

    public EpisodeImmutableSetupGeneric(
            TState initialState,
            Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> playerPaperPolicy,
            Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> opponentPolicy,
            int stepCountLimit) {
        this.initialState = initialState;
        this.playerPaperPolicy = playerPaperPolicy;
        this.opponentPolicy = opponentPolicy;
        this.stepCountLimit = stepCountLimit;
    }

    public TState getInitialState() {
        return initialState;
    }

    public Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getPlayerPaperPolicy() {
        return playerPaperPolicy;
    }

    public Policy<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> getOpponentPolicy() {
        return opponentPolicy;
    }

    public int getStepCountLimit() {
        return stepCountLimit;
    }
}
