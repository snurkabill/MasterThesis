package vahy.paperGenerics.reinforcement;

import vahy.impl.model.observation.DoubleVector;
import vahy.impl.predictor.DataTablePredictor;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.List;

public class DataTablePredictorWithLr<TObservation extends DoubleVector> extends DataTablePredictor<TObservation> {

    private final int actionCount;
    private final double learningRate;

    public DataTablePredictorWithLr(double[] defaultPrediction, double learningRate, int actionCount) {
        super(defaultPrediction);
        this.actionCount = actionCount;
        if(learningRate <= 0.0) {
            throw new IllegalArgumentException("Learning rate must be positive. Value: [" + learningRate + "]");
        }
        this.learningRate = learningRate;
    }

    @Override
    public void train(List<ImmutableTuple<TObservation, double[]>> episodeData) {
        for (ImmutableTuple<TObservation, double[]> entry : episodeData) {
            double[] newSampledPrediction = entry.getSecond();

            double[] defaultPredictionCopy = new double[defaultPrediction.length];
            System.arraycopy(defaultPrediction, 0, defaultPredictionCopy, 0, defaultPrediction.length);
            double[] prediction = predictionMap.getOrDefault(entry.getFirst(), defaultPredictionCopy);

            var rewardDiff = prediction[0] - newSampledPrediction[0];
            prediction[0] = prediction[0] - learningRate * rewardDiff;

            var riskDiff = prediction[1] - newSampledPrediction[1];
            prediction[1] = prediction[1] - learningRate * riskDiff;

            double[] killMeNow = new double[actionCount];
            for (int i = 0; i < actionCount; i++) {
                var diffByI = - newSampledPrediction[i + 2] / prediction[i + 2];
                killMeNow[i] = prediction[i + 2] - learningRate * diffByI;
            }
            RandomDistributionUtils.applySoftmax(killMeNow);
            System.arraycopy(killMeNow, 0, prediction, 2, actionCount);

            predictionMap.put(entry.getFirst(), prediction);
        }
    }
}
