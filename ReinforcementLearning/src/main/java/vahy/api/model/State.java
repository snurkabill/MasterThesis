package vahy.api.model;

import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;

import java.util.List;

public interface State<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>> extends Observation {

    TAction[] getAllPossibleActions();

    TAction[] getPossiblePlayerActions();

    TAction[] getPossibleOpponentActions();

    StateRewardReturn<TAction, TPlayerObservation, TOpponentObservation, TState> applyAction(TAction actionType);

    TPlayerObservation getPlayerObservation();

    TOpponentObservation getOpponentObservation();

    Predictor<TState> getKnownModelWithPerfectObservationPredictor();

    String readableStringRepresentation();

    List<String> getCsvHeader();

    List<String> getCsvRecord();

    boolean isOpponentTurn();

    default boolean isPlayerTurn() {
        return !isOpponentTurn();
    }

    boolean isFinalState();

}
