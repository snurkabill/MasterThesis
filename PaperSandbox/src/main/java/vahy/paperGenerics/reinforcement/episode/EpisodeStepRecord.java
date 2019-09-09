package vahy.paperGenerics.reinforcement.episode;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.paperGenerics.PaperState;

public class EpisodeStepRecord<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    private final boolean isPlayerMove;
    private final TAction playedAction;
    private final PolicyStepRecord policyStepRecord;
    private final TState fromState;
    private final TState toState;
    private final double reward;

    public EpisodeStepRecord(boolean isPlayerMove, TAction playedAction, PolicyStepRecord policyStepRecord, TState fromState, TState toState, double reward) {
        this.isPlayerMove = isPlayerMove;
        this.playedAction = playedAction;
        this.policyStepRecord = policyStepRecord;
        this.fromState = fromState;
        this.toState = toState;
        this.reward = reward;
    }

    public boolean isPlayerMove() {
        return isPlayerMove;
    }

    public TAction getPlayedAction() {
        return playedAction;
    }

    public PolicyStepRecord getPolicyStepRecord() {
        return policyStepRecord;
    }

    public TState getFromState() {
        return fromState;
    }

    public TState getToState() {
        return toState;
    }

    public double getReward() {
        return reward;
    }
}
