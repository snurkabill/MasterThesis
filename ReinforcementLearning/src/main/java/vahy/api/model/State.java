package vahy.api.model;

import vahy.api.model.observation.Observation;
import vahy.api.predictor.PerfectStatePredictor;

import java.util.List;

public interface
State<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends State<TAction, TObservation, TState>> {

    TAction[] getAllPossibleActions(int inGameEntityId);

    int getTotalEntityCount();

    StateRewardReturn<TAction, TObservation, TState> applyAction(TAction actionType);

    TObservation getInGameEntityObservation(int inGameEntityId);

    TObservation getCommonObservation(int inGameEntityId);

    PerfectStatePredictor<TAction, TObservation, TState> getKnownModelWithPerfectObservationPredictor();

    String readableStringRepresentation();

    List<String> getCsvHeader();

    List<String> getCsvRecord();

    int getInGameEntityIdOnTurn();

    boolean isEnvironmentEntityOnTurn();

    boolean isInGame(int inGameEntityId);

    boolean isFinalState();

}
