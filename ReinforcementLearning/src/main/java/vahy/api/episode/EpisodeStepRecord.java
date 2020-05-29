package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;

import java.util.List;

public interface EpisodeStepRecord<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>, TPolicyRecord extends PolicyRecord> {

    int getPolicyIdOnTurn();

    int getInGameEntityIdOnTurn();

    TAction getAction();

    TPolicyRecord getPolicyStepRecord();

    TState getFromState();

    TState getToState();

    double[] getReward();

    String toLogString();

    List<String> getCsvHeader();

    List<String> getCsvRecord();
}
