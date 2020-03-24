package vahy.impl.episode;

import vahy.api.episode.EpisodeStepRecord;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;

import java.util.ArrayList;
import java.util.List;

public class EpisodeStepRecordImpl<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord>
    implements EpisodeStepRecord<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> {

    private final boolean isPlayerMove;
    private final TAction playedAction;
    private final TPolicyRecord policyStepRecord;
    private final TState fromState;
    private final TState toState;
    private final double reward;

    public EpisodeStepRecordImpl(boolean isPlayerMove,
                                 TAction playedAction,
                                 TPolicyRecord policyStepRecord,
                                 TState fromState,
                                 TState toState,
                                 double reward) {
        this.isPlayerMove = isPlayerMove;
        this.playedAction = playedAction;
        this.policyStepRecord = policyStepRecord;
        this.fromState = fromState;
        this.toState = toState;
        this.reward = reward;
    }

    @Override
    public boolean isPlayerMove() {
        return isPlayerMove;
    }

    @Override
    public TAction getPlayedAction() {
        return playedAction;
    }

    @Override
    public TPolicyRecord getPolicyStepRecord() {
        return policyStepRecord;
    }

    @Override
    public TState getFromState() {
        return fromState;
    }

    @Override
    public TState getToState() {
        return toState;
    }

    @Override
    public double getReward() {
        return reward;
    }

    @Override
    public String toString() {
        return "EpisodeStepRecord{" +
            "isPlayerMove=" + isPlayerMove +
            ", playedAction=" + playedAction +
            ", policyStepRecord=" + policyStepRecord.toString() +
            ", fromState=" + fromState +
            ", toState=" + toState +
            ", reward=" + reward +
            '}';
    }

    @Override
    public String toLogString() {
        StringBuilder sb = new StringBuilder();
        sb.append("action: ");
        sb.append(getPlayedAction());
        sb.append(" getting reward: ");
        sb.append(getReward());
        sb.append(". PolicyStepLog: ");
        sb.append(policyStepRecord.toLogString());
        return sb.toString();
    }

    @Override
    public List<String> getCsvHeader() {
        var list = new ArrayList<String>();
        list.add("Is player move");
        list.add("Action played");
        list.add("Obtained reward");
        if(isPlayerMove) {
            list.addAll(policyStepRecord.getCsvHeader());
        }
        return list;
    }

    @Override
    public List<String> getCsvRecord() {
        var list = new ArrayList<String>();
        list.add(Boolean.toString(isPlayerMove));
        list.add(playedAction.toString());
        list.add(Double.toString(reward));
        if(isPlayerMove) {
            list.addAll(policyStepRecord.getCsvRecord());
        }
        return list;
    }
}

