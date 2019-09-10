package vahy.paperGenerics.reinforcement;

import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.HashMap;
import java.util.List;

public class DataTableApproximatorWithLr<TObservation extends DoubleVector> extends TrainableApproximator<TObservation> {

    private final int actionCount;
    private final double[] defaultPrediction;

    private final double learningRate;

    public DataTableApproximatorWithLr(int actionCount, double learningRate) {
        super(null);
        this.actionCount = actionCount;
        if(learningRate <= 0.0) {
            throw new IllegalArgumentException("Learning rate must be positive. Value: [" + learningRate + "]");
        }
        this.learningRate = learningRate;
        this.defaultPrediction = new double[2 + actionCount];
        this.defaultPrediction[0] = 0;
        this.defaultPrediction[1] = 0.0;
        for (int i = 0; i < actionCount; i++) {
            defaultPrediction[i + 2] = 1.0 / actionCount;
        }
    }

    private HashMap<TObservation, double[]> predictionMap = new HashMap<>();

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

//        predictionMap = episodeData.stream().collect(Collectors.toMap(ImmutableTuple::getFirst, ImmutableTuple::getSecond, (oldValue, newValue) -> oldValue, HashMap::new));
    }

    @Override
    public double[] apply(TObservation doubleVectorialObservation) {
        return predictionMap.getOrDefault(doubleVectorialObservation, defaultPrediction);
    }
}
