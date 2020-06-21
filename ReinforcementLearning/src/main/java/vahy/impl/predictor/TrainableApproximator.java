package vahy.impl.predictor;

import vahy.api.learning.model.SupervisedTrainableModel;
import vahy.api.predictor.TrainablePredictor;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class TrainableApproximator implements TrainablePredictor {

    private final SupervisedTrainableModel supervisedTrainableModel;

    public TrainableApproximator(SupervisedTrainableModel supervisedTrainableModel) {
        this.supervisedTrainableModel = supervisedTrainableModel;
    }

    @Override
    public void train(List<ImmutableTuple<DoubleVector, double[]>> data) {
        double[][] input = new double[data.size()][];
        double[][] target = new double[data.size()][];
        Iterator<ImmutableTuple<DoubleVector, double[]>> iterator = data.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            ImmutableTuple<DoubleVector, double[]> next = iterator.next();
            input[i] = next.getFirst().getObservedVector();
            target[i] = next.getSecond();
        }
        supervisedTrainableModel.fit(input, target);
    }

    @Override
    public void train(ImmutableTuple<DoubleVector[], double[][]> data) {
        var inputArray = data.getFirst();
        double[][] input = new double[inputArray.length][];
        for (int i = 0; i < input.length; i++) {
            input[i] = inputArray[i].getObservedVector();
        }
        supervisedTrainableModel.fit(input, data.getSecond());
    }

    @Override
    public double[] apply(DoubleVector doubleVectorialObservation) {
        return supervisedTrainableModel.predict(doubleVectorialObservation.getObservedVector());
    }

    @Override
    public double[][] apply(DoubleVector[] doubleVectorialObservationArray) {
        double[][] input = new double[doubleVectorialObservationArray.length][];
        for (int i = 0; i < doubleVectorialObservationArray.length; i++) {
            input[i] = doubleVectorialObservationArray[i].getObservedVector();
        }
        return supervisedTrainableModel.predict(input);
    }

    @Override
    public List<double[]> apply(List<DoubleVector> doubleVectors) {
        double[][] inputArray = new double[doubleVectors.size()][];
        for (int i = 0; i < doubleVectors.size(); i++) {
            inputArray[i] = doubleVectors.get(i).getObservedVector();
        }
        var output = supervisedTrainableModel.predict(inputArray);
        var outputList = new ArrayList<double[]>(output.length);
        outputList.addAll(Arrays.asList(output));
        return outputList;
    }

    @Override
    public void close() throws IOException {
        supervisedTrainableModel.close();
    }
}
