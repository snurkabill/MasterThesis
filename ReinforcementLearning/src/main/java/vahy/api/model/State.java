package vahy.api.model;

import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;

import java.util.List;

public interface State<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>> extends Observation {

    TAction[] getAllPossibleActions();

    StateRewardReturn<TAction, TObservation, TState> applyAction(TAction actionType);

    TObservation getInGameEntityObservation(int inGameEntityId);

    TObservation getCommonObservation(int inGameEntityId);

    Predictor<TState> getKnownModelWithPerfectObservationPredictor();

    String readableStringRepresentation();

    List<String> getCsvHeader();

    List<String> getCsvRecord();

    int getTotalPlayerCount();

    int getInGameEntityIdOnTurn();

    boolean isInGame(int inGameEntityId);

    boolean isFinalState();

}
