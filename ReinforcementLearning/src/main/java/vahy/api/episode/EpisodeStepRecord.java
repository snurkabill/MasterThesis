package vahy.api.episode;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;

import java.util.List;

public interface EpisodeStepRecord<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends State<TAction, TObservation, TState>> {

    int getPolicyIdOnTurn();

    int getInGameEntityIdOnTurn();

    TAction getAction();

    PolicyRecord getPolicyStepRecord();

    TState getFromState();

    TState getToState();

    double[] getReward();

    String toLogString();

    List<String> getCsvHeader();

    List<String> getCsvRecord();
}
