package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.StateWrapper;
import vahy.api.model.observation.Observation;

import java.util.List;

public interface Policy<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>, TPolicyRecord extends PolicyRecord> {

    int getPolicyId();

    double[] getActionProbabilityDistribution(StateWrapper<TAction, TObservation, TState> gameState);

    TAction getDiscreteAction(StateWrapper<TAction, TObservation, TState> gameState);

    void updateStateOnPlayedActions(List<TAction> opponentActionList);

    TPolicyRecord getPolicyRecord(StateWrapper<TAction, TObservation, TState> gameState);

}
