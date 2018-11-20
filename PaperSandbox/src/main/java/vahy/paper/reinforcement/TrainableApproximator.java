package vahy.paper.reinforcement;

import vahy.api.learning.model.SupervisedTrainableModel;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.utils.ImmutableTuple;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class TrainableApproximator implements Function<DoubleVectorialObservation, double[]> {

    private final SupervisedTrainableModel supervisedTrainableModel;

    public TrainableApproximator(SupervisedTrainableModel supervisedTrainableModel) {
        this.supervisedTrainableModel = supervisedTrainableModel;
    }

    public void train(List<ImmutableTuple<DoubleVectorialObservation, double[]>> episodeData) {
        double[][] input = new double[episodeData.size()][];
        double[][] target = new double[episodeData.size()][];
        Iterator<ImmutableTuple<DoubleVectorialObservation, double[]>> iterator = episodeData.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            ImmutableTuple<DoubleVectorialObservation, double[]> next = iterator.next();
            input[i] = next.getFirst().getObservedVector();
            target[i] = next.getSecond();
        }
        supervisedTrainableModel.fit(input, target);
    }

    @Override
    public double[] apply(DoubleVectorialObservation doubleVectorialObservation) {
        return supervisedTrainableModel.predict(doubleVectorialObservation.getObservedVector());
    }
}
