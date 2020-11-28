package vahy.api.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import vahy.examples.testdomain.emptySpace.EmptySpaceAction;
import vahy.examples.testdomain.emptySpace.EmptySpaceState;
import vahy.impl.model.observation.DoubleVector;

public class StateWrapperTest {

    private StateWrapperTest() {}

    @Test
    public void stateWrapperObservationAggregationInitializeTest() {

        var state = new EmptySpaceState(true, true);
        var stateWrapper = new StateWrapper<EmptySpaceAction, DoubleVector, EmptySpaceState>(0, 1,  state);
        Assertions.assertArrayEquals(stateWrapper.getObservation().getObservedVector(), new double[] {1.0, 1.0});

        stateWrapper = new StateWrapper<EmptySpaceAction, DoubleVector, EmptySpaceState>(0, 2,  state);
        Assertions.assertArrayEquals(stateWrapper.getObservation().getObservedVector(), new double[] {1.0, 1.0, 1.0, 1.0});

        stateWrapper = new StateWrapper<EmptySpaceAction, DoubleVector, EmptySpaceState>(0, 3,  state);
        Assertions.assertArrayEquals(stateWrapper.getObservation().getObservedVector(), new double[] {1.0, 1.0, 1.0, 1.0, 1.0, 1.0});

        state = new EmptySpaceState(false, true);
        stateWrapper = new StateWrapper<EmptySpaceAction, DoubleVector, EmptySpaceState>(0, 1,  state);
        Assertions.assertArrayEquals(stateWrapper.getObservation().getObservedVector(), new double[] {0.0, 1.0});

        stateWrapper = new StateWrapper<EmptySpaceAction, DoubleVector, EmptySpaceState>(0, 2,  state);
        Assertions.assertArrayEquals(stateWrapper.getObservation().getObservedVector(), new double[] {0.0, 1.0, 0.0, 1.0});

        stateWrapper = new StateWrapper<EmptySpaceAction, DoubleVector, EmptySpaceState>(0, 3,  state);
        Assertions.assertArrayEquals(stateWrapper.getObservation().getObservedVector(), new double[] {0.0, 1.0, 0.0, 1.0, 0.0, 1.0});
    }

    @Test
    public void stateWrapperObservationAggregationApplyTest() {

        var state = new EmptySpaceState(true, true);
        var stateWrapper = new StateWrapper<EmptySpaceAction, DoubleVector, EmptySpaceState>(0, 1,  state);
        var nextStateWrapper = stateWrapper.applyAction(EmptySpaceAction.A).getState();
        Assertions.assertArrayEquals(nextStateWrapper.getObservation().getObservedVector(), new double[] {0.0, 0.0});
        nextStateWrapper = nextStateWrapper.applyAction(EmptySpaceAction.A).getState();
        Assertions.assertArrayEquals(nextStateWrapper.getObservation().getObservedVector(), new double[] {1.0, 0.0});
        nextStateWrapper = nextStateWrapper.applyAction(EmptySpaceAction.A).getState();
        Assertions.assertArrayEquals(nextStateWrapper.getObservation().getObservedVector(), new double[] {0.0, 1.0});

        stateWrapper = new StateWrapper<EmptySpaceAction, DoubleVector, EmptySpaceState>(0, 3,  state);
        nextStateWrapper = stateWrapper.applyAction(EmptySpaceAction.A).getState();
        Assertions.assertArrayEquals(nextStateWrapper.getObservation().getObservedVector(), new double[] {1.0, 1.0, 1.0, 1.0, 0.0, 0.0});
        nextStateWrapper = nextStateWrapper.applyAction(EmptySpaceAction.A).getState();
        Assertions.assertArrayEquals(nextStateWrapper.getObservation().getObservedVector(), new double[] {1.0, 1.0, 0.0, 0.0, 1.0, 0.0});
        nextStateWrapper = nextStateWrapper.applyAction(EmptySpaceAction.A).getState();
        Assertions.assertArrayEquals(nextStateWrapper.getObservation().getObservedVector(), new double[] {0.0, 0.0, 1.0, 0.0, 0.0, 1.0});

    }


}
