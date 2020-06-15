package vahy.impl.search.AlphaZero;

import vahy.impl.model.observation.DoubleVector;
import vahy.impl.predictor.DataTablePredictor;
import vahy.utils.ImmutableTuple;
import vahy.utils.RandomDistributionUtils;

import java.util.List;

public class AlphaZeroTablePredictor extends DataTablePredictor {

    private final int entityCount;
    private final int actionCount;
    private final double learningRate;
    private final double[] killMeNow;

    public AlphaZeroTablePredictor(double[] defaultPrediction, int entityCount, double learningRate, int actionCount) {
        super(defaultPrediction);
        this.entityCount = entityCount;
        this.actionCount = actionCount;
        if(learningRate <= 0.0) {
            throw new IllegalArgumentException("Learning rate must be positive. Value: [" + learningRate + "]");
        }
        this.learningRate = learningRate;
        this.killMeNow = new double[actionCount];
    }

    private void trainDataSample(DoubleVector observation, double[] target) {
        double[] prediction = predictionMap.getOrDefault(observation, defaultPrediction);
        double[] newPrediction = new double[prediction.length];

        for (int i = 0; i < entityCount; i++) {
            var rewardDiff = prediction[i] - target[i];
            newPrediction[i] = prediction[i] - learningRate * rewardDiff;
        }

        for (int i = entityCount, j = 0; j < actionCount; i++, j++) {
            var diffByI = - target[i] / prediction[i];
            killMeNow[j] = prediction[i] - learningRate * diffByI;
        }
        RandomDistributionUtils.applySoftmax(killMeNow);
        System.arraycopy(killMeNow, 0, newPrediction, entityCount, actionCount);

        predictionMap.put(observation, newPrediction);
    }

    @Override
    public void train(List<ImmutableTuple<DoubleVector, double[]>> data) {
        for (ImmutableTuple<DoubleVector, double[]> entry : data) {
            trainDataSample(entry.getFirst(), entry.getSecond());
        }
    }

    @Override
    public void train(ImmutableTuple<DoubleVector[], double[][]> data) {
        var observations = data.getFirst();
        var targets = data.getSecond();
        for (int i = 0; i < observations.length; i++) {
            trainDataSample(observations[i], targets[i]);
        }
    }
}
