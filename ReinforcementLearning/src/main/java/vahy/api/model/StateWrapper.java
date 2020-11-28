package vahy.api.model;

import vahy.api.model.observation.Observation;
import vahy.api.predictor.PerfectStatePredictor;
import vahy.impl.model.ImmutableStateWrapperRewardReturn;

import java.util.ArrayDeque;

public class StateWrapper<TAction extends Enum<TAction> & Action, TObservation extends Observation<TObservation>, TState extends State<TAction, TObservation, TState>> {

    protected final int inGameEntityId;
    protected final int stateBufferSize;
    protected final ArrayDeque<TObservation> stateBuffer;
    protected final TState state;

    private static <TObservation> ArrayDeque<TObservation> fillBufferWithInitialState(int stateBufferSize, TObservation observation) {
        var buffer = new ArrayDeque<TObservation>(stateBufferSize);
        for (int i = 0; i < stateBufferSize; i++) {
            buffer.add(observation);
        }
        return buffer;
    }

//    private static <TState> ArrayDeque<TState> fillBufferWithInitialState(int stateBufferSize, TState[] stateArray) {
//        if(stateArray.length > stateBufferSize) {
//            throw new IllegalStateException("StateArray is larger [" + stateArray.length + "] than stateBufferSize: [" + stateBufferSize +"]");
//        }
//        var buffer = new ArrayDeque<TState>(stateBufferSize);
//
//        if(stateBufferSize > stateArray.length) {
//            for (int i = 0; i < stateBufferSize - stateArray.length; i++) {
//                buffer.add(stateArray[0]);
//            }
//        }
//        for (int i = 1; i < stateArray.length; i++) {
//            buffer.add(stateArray[i]);
//        }
//        return buffer;
//    }

    private static <TObservation> ArrayDeque<TObservation> fillBufferWithInitialState(int stateBufferSize, TObservation observation, ArrayDeque<TObservation> previousBuffer) {
        var buffer = new ArrayDeque<TObservation>(stateBufferSize);
        buffer.addAll(previousBuffer);
        if(buffer.size() == stateBufferSize) {
            buffer.pollFirst();
        }
        buffer.add(observation);
        return buffer;
    }

    public StateWrapper(int inGameEntityId, TState state) {
        this(inGameEntityId, 1, state);
    }

    public StateWrapper(int inGameEntityId, TState state, StateWrapper<TAction, TObservation, TState> previousWrapper) {
        this(inGameEntityId, previousWrapper.stateBufferSize, state, fillBufferWithInitialState(previousWrapper.stateBufferSize, state.getInGameEntityObservation(inGameEntityId), previousWrapper.stateBuffer));
    }

    public StateWrapper(int inGameEntityId, int aggregateObservationCount, TState state) {
        this(inGameEntityId, aggregateObservationCount, state, fillBufferWithInitialState(aggregateObservationCount, state.getInGameEntityObservation(inGameEntityId)));
    }

//    public StateWrapper(int inGameEntityId, int aggregateObservationCount, TState[] stateArray) {
//        this(inGameEntityId, aggregateObservationCount, stateArray[stateArray.length - 1], fillBufferWithInitialState(aggregateObservationCount, stateArray));
//    }

    private StateWrapper(int inGameEntityId, int stateBufferSize, TState state, ArrayDeque<TObservation> stateBuffer) {
        this.inGameEntityId = inGameEntityId;
        this.stateBufferSize = stateBufferSize;
        this.stateBuffer = stateBuffer;
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
        var newBuffer = new ArrayDeque<TObservation>(stateBuffer);
        newBuffer.pollFirst();
        newBuffer.add(stateRewardReturn.getState().getInGameEntityObservation(inGameEntityId));
        return new ImmutableStateWrapperRewardReturn<>(
            new StateWrapper<>(inGameEntityId, stateBufferSize, stateRewardReturn.getState(), newBuffer),
            allPlayerRewards[inGameEntityId],
            allPlayerRewards,
            allObservedActionsByPlayer[inGameEntityId]);
    }

    public TObservation getObservation() {
        if(stateBufferSize == 1) {
            return state.getInGameEntityObservation(inGameEntityId);
        } else {
            // TODO: calling observation just to get the method is shitty.
            var observation = stateBuffer.getFirst();
            return observation.groupArrayOfObservations(stateBuffer);
        }
    }

    public TObservation getCommonObservation() {
        throw new IllegalStateException("Common observation is missing implementation in stateWrapper. Just add another buffer.");
//        if(stateBufferSize == 1) {
//            return state.getCommonObservation(inGameEntityId);
//        } else {
//            return state.getCommonObservation(inGameEntityId).groupListOfObservations(stateBuffer.stream().map(x -> x.getCommonObservation(inGameEntityId)).collect(Collectors.toList()));
//        }
    }

    public PerfectStatePredictor<TAction, TObservation, TState> getKnownModelWithPerfectObservationPredictor() {
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
