package vahy.api.model;

import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.impl.model.ImmutableStateWrapperRewardReturn;

public class StateWrapper<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>> {

    protected final int policyId;
    protected final TState state;

    public StateWrapper(int policyId, TState state) {
        this.policyId = policyId;
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
        return new ImmutableStateWrapperRewardReturn<>(new StateWrapper<>(policyId, stateRewardReturn.getState()), stateRewardReturn.getReward()[policyId]);
    }

    public TObservation getObservation() {
        return state.getPlayerObservation(policyId);
    }

    public TObservation getCommonObservation() {
        return state.getCommonObservation(policyId);
    }

    public Predictor<TState> getKnownModelWithPerfectObservationPredictor() {
        return state.getKnownModelWithPerfectObservationPredictor();
    }

    public int getPlayerIdWrapper() {
        return policyId;
    }

    public boolean isPlayerTurn() {
        return state.getPlayerIdOnTurn() == policyId;
    }

    public int getPlayerOnTurnId() {
        return state.getPlayerIdOnTurn();
    }

    public boolean isFinalState() {
        if(state.isFinalState()) {
            return true;
        }
        return state.isInGame(policyId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateWrapper<?, ?, ?> that = (StateWrapper<?, ?, ?>) o;

        if (policyId != that.policyId) return false;
        return state.equals(that.state);
    }

    @Override
    public int hashCode() {
        int result = policyId;
        result = 31 * result + state.hashCode();
        return result;
    }
}
