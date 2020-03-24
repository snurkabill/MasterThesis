package vahy.api.policy;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.model.State;

import java.util.List;

public interface Policy<
        TAction extends Action,
        TPlayerObservation extends Observation,
        TOpponentObservation extends Observation,
        TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
        TPolicyRecord extends PolicyRecord> {

    double[] getActionProbabilityDistribution(TState gameState);

    TAction getDiscreteAction(TState gameState);

    void updateStateOnPlayedActions(List<TAction> opponentActionList);

    TPolicyRecord getPolicyRecord(TState gameState);

}
