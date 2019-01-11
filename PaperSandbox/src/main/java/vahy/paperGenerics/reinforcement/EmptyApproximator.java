package vahy.paperGenerics.reinforcement;

import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.List;

public class EmptyApproximator<TObservation extends DoubleVector> extends TrainableApproximator<TObservation> {

    private static final double[] staticPrediction = new double[] {0.0, 0.0, 1.0/3.0, 1.0/3.0, 1.0/3.0};

    public EmptyApproximator() {
        super(null);
    };

    @Override
    public void train(List<ImmutableTuple<TObservation, double[]>> episodeData) {
    }

    @Override
    public double[] apply(TObservation doubleVectorialObservation) {
        return staticPrediction;
    }
}
