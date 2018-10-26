package vahy.paper.reinforcement;

import vahy.impl.model.observation.DoubleVectorialObservation;

import java.util.function.Function;

public class MonteCarloRolloutNodeEvaluator implements Function<DoubleVectorialObservation, double[]> {



    @Override
    public double[] apply(DoubleVectorialObservation doubleVectorialObservation) {
        return new double[0];
    }
}
