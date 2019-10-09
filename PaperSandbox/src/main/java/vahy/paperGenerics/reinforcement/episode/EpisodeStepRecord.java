package vahy.paperGenerics.reinforcement.episode;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.PaperPolicyRecord;

import java.util.ArrayList;
import java.util.List;

public class EpisodeStepRecord<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>> {

    private final boolean isPlayerMove;
    private final TAction playedAction;
    private final PaperPolicyRecord paperPolicyRecord;
    private final TState fromState;
    private final TState toState;
    private final double reward;

    public EpisodeStepRecord(boolean isPlayerMove, TAction playedAction, PaperPolicyRecord paperPolicyRecord, TState fromState, TState toState, double reward) {
        this.isPlayerMove = isPlayerMove;
        this.playedAction = playedAction;
        this.paperPolicyRecord = paperPolicyRecord;
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

    public PaperPolicyRecord getPaperPolicyRecord() {
        return paperPolicyRecord;
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

    @Override
    public String toString() {
        return "EpisodeStepRecord{" +
                "isPlayerMove=" + isPlayerMove +
                ", playedAction=" + playedAction +
                ", policyStepRecord=" + paperPolicyRecord.toString() +
                ", fromState=" + fromState +
                ", toState=" + toState +
                ", reward=" + reward +
                '}';
    }

    public List<String> getCsvHeader() {
        var list = new ArrayList<String>();
        list.add("Is player move");
        list.add("Action played");
        list.add("Obtained reward");
        if(isPlayerMove) {
            list.addAll(paperPolicyRecord.getCsvHeader());
        }
        return list;
    }

    public List<String> getCsvRecord() {
        var list = new ArrayList<String>();
        list.add(Boolean.toString(isPlayerMove));
        list.add(playedAction.toString());
        list.add(Double.toString(reward));
        if(isPlayerMove) {
            list.addAll(paperPolicyRecord.getCsvRecord());
        }
        return list;
    }
}
