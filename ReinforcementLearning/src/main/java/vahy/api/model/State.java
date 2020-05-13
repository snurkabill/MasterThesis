package vahy.api.model;

import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;

import java.util.List;

public interface State<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>> extends Observation {

    TAction[] getAllPossibleActions();

    StateRewardReturn<TAction, TObservation, TState> applyAction(TAction actionType);

    TObservation getPlayerObservation(int playerId);

    TObservation getCommonObservation(int playerId);

    Predictor<TState> getKnownModelWithPerfectObservationPredictor();

    String readableStringRepresentation();

    List<String> getCsvHeader();

    List<String> getCsvRecord();

    TAction[] getAllEnvironmentActions();

    TAction[] getAllPlayerActions();

    int getTotalPlayerCount();

    int getPlayerIdOnTurn();

    boolean isInGame(int playerId);

    boolean isFinalState();

}
