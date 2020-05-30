package vahy.api.model;

import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.impl.model.ImmutableStateWrapperRewardReturn;

public class StateWrapper<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>> {

    protected final int inGameEntityId;
    protected final TState state;

    public StateWrapper(int inGameEntityId, TState state) {
        this.inGameEntityId = inGameEntityId;
        this.state = state;
    }

    // TODO: remove this
    public TState getWrappedState() {
        return state;
    }

    public TAction[] getAllPossibleActions() {
        return state.getAllPossibleActions();
    }

    public StateWrapperRewardReturn<TAction, TObservation, TState> applyAction(TAction actionType) {
        StateRewardReturn<TAction, TObservation, TState> stateRewardReturn = state.applyAction(actionType);
        return new ImmutableStateWrapperRewardReturn<>(new StateWrapper<>(inGameEntityId, stateRewardReturn.getState()), stateRewardReturn.getReward()[inGameEntityId]);
    }

    public TObservation getObservation() {
        return state.getInGameEntityObservation(inGameEntityId);
    }

    public TObservation getCommonObservation() {
        return state.getCommonObservation(inGameEntityId);
    }

    public Predictor<TState> getKnownModelWithPerfectObservationPredictor() {
        return state.getKnownModelWithPerfectObservationPredictor();
    }

    public int getinGameEntityIdWrapper() {
        return inGameEntityId;
    }

    public boolean isPlayerTurn() {
        return state.getInGameEntityIdOnTurn() == inGameEntityId;
    }

    public int getPlayerOnTurnId() {
        return state.getInGameEntityIdOnTurn();
    }

    public boolean isFinalState() {
        if(state.isFinalState()) {
            return true;
        }
        return state.isInGame(inGameEntityId);
    }

    public String getReadableStringRepresentation() {
        return state.readableStringRepresentation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateWrapper<?, ?, ?> that = (StateWrapper<?, ?, ?>) o;

        if (inGameEntityId != that.inGameEntityId) return false;
        return state.equals(that.state);
    }

    @Override
    public int hashCode() {
        int result = inGameEntityId;
        result = 31 * result + state.hashCode();
        return result;
    }
}
