package vahy.examples.patrolling;

import vahy.api.model.State;
import vahy.api.model.StateRewardReturn;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.impl.model.observation.DoubleVector;

import java.util.List;

public class PatrollingState implements State<PatrollingAction, DoubleVector, PatrollingState>, Observation {


    private final PatrollingStaticPart staticPart;
    private final int[] agentPosition;
    private final int attackCountDown;

    public PatrollingState(PatrollingStaticPart staticPart, int[] agentPosition, int attackCountDown) {
        this.staticPart = staticPart;
        this.agentPosition = agentPosition;
        this.attackCountDown = attackCountDown;
    }

    @Override
    public PatrollingAction[] getAllPossibleActions() {
        return new PatrollingAction[0];
    }

    @Override
    public int getTotalEntityCount() {
        return 2;
    }

    @Override
    public StateRewardReturn<PatrollingAction, DoubleVector, PatrollingState> applyAction(PatrollingAction actionType) {
        return null;
    }

    @Override
    public DoubleVector getInGameEntityObservation(int inGameEntityId) {
        return null;
    }

    @Override
    public DoubleVector getCommonObservation(int inGameEntityId) {
        return null;
    }

    @Override
    public Predictor<PatrollingState> getKnownModelWithPerfectObservationPredictor() {
        return null;
    }

    @Override
    public String readableStringRepresentation() {
        return null;
    }

    @Override
    public List<String> getCsvHeader() {
        return null;
    }

    @Override
    public List<String> getCsvRecord() {
        return null;
    }

    @Override
    public int getInGameEntityIdOnTurn() {
        return 0;
    }

    @Override
    public boolean isEnvironmentEntityOnTurn() {
        return false;
    }

    @Override
    public boolean isInGame(int inGameEntityId) {
        return false;
    }

    @Override
    public boolean isFinalState() {
        return false;
    }
}
