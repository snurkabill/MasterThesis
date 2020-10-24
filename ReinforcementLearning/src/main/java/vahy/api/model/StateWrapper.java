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
        return state.getAllPossibleActions(inGameEntityId);
    }

    public StateWrapperRewardReturn<TAction, TObservation, TState> applyAction(TAction actionType) {
        StateRewardReturn<TAction, TObservation, TState> stateRewardReturn = state.applyAction(actionType);
        var allPlayerRewards = stateRewardReturn.getReward();
        var allObservedActionsByPlayer = stateRewardReturn.getAction();
        return new ImmutableStateWrapperRewardReturn<>(new StateWrapper<>(inGameEntityId, stateRewardReturn.getState()), allPlayerRewards[inGameEntityId], allPlayerRewards, allObservedActionsByPlayer[inGameEntityId]);
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

    public int getInGameEntityId() {
        return inGameEntityId;
    }

    public boolean isPlayerTurn() {
        return state.getInGameEntityIdOnTurn() == inGameEntityId;
    }

    public int getTotalEntityCount() {
        return state.getTotalEntityCount();
    }

    public int getInGameEntityOnTurnId() {
        return state.getInGameEntityIdOnTurn();
    }

    public boolean isEnvironmentEntityOnTurn() {
        return state.isEnvironmentEntityOnTurn();
    }

    public boolean isFinalState() {
        if(state.isFinalState()) {
            return true;
        }
        return !state.isInGame(inGameEntityId);
    }

    public String getReadableStringRepresentation() {
        return state.readableStringRepresentation();
    }

    public boolean wrappedStatesEquals(Object o) {
        StateWrapper<?, ?, ?> that = (StateWrapper<?, ?, ?>) o;

        if (inGameEntityId != that.inGameEntityId) return false;
        return state.equals(that.state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (!(o instanceof StateWrapper)) {
            return false;
        }

        StateWrapper<?, ?, ?> that = (StateWrapper<?, ?, ?>) o;

        if (inGameEntityId != that.inGameEntityId){
            return false;
        }
        return state.equals(that.state);
    }

    @Override
    public int hashCode() {
        int result = inGameEntityId;
        result = 31 * result + state.hashCode();
        return result;
    }
}
