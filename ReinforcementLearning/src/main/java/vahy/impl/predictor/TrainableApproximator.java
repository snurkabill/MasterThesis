package vahy.impl.predictor;

import vahy.api.learning.model.SupervisedTrainableModel;
import vahy.api.predictor.TrainablePredictor;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.Iterator;
import java.util.List;

public class TrainableApproximator<TObservation extends DoubleVector> implements TrainablePredictor<TObservation> {

    private final SupervisedTrainableModel supervisedTrainableModel;

    public TrainableApproximator(SupervisedTrainableModel supervisedTrainableModel) {
        this.supervisedTrainableModel = supervisedTrainableModel;
    }

    @Override
    public void train(List<ImmutableTuple<TObservation, double[]>> episodeData) {
        double[][] input = new double[episodeData.size()][];
        double[][] target = new double[episodeData.size()][];
        Iterator<ImmutableTuple<TObservation, double[]>> iterator = episodeData.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            ImmutableTuple<TObservation, double[]> next = iterator.next();
            input[i] = next.getFirst().getObservedVector();
            target[i] = next.getSecond();
        }
        supervisedTrainableModel.fit(input, target);
    }

    @Override
    public double[] apply(TObservation doubleVectorialObservation) {
        return supervisedTrainableModel.predict(doubleVectorialObservation.getObservedVector());
    }

    @Override
    public double[][] apply(TObservation[] doubleVectorialObservationArray) {
        double[][] input = new double[doubleVectorialObservationArray.length][];
        for (int i = 0; i < doubleVectorialObservationArray.length; i++) {
            input[i] = doubleVectorialObservationArray[i].getObservedVector();
        }
        return supervisedTrainableModel.predict(input);
    }
}
