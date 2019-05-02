package vahy.paperGenerics.reinforcement;

import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DataTableApproximator<TObservation extends DoubleVector> extends TrainableApproximator<TObservation> {

    private final int actionCount;
    private final double[] defaultPrediction;
    private final boolean omitProbabilities;
    public DataTableApproximator(int actionCount, boolean omitProbabilities) {
        super(null);
        this.actionCount = actionCount;
        this.omitProbabilities = omitProbabilities;

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
        predictionMap = episodeData
            .stream()
            .collect(Collectors
                .toMap(
                    ImmutableTuple::getFirst,
                    ImmutableTuple::getSecond,
                    (oldValue, newValue) -> oldValue,
                    HashMap::new)
            );
    }

    @Override
    public double[] apply(TObservation doubleVectorialObservation) {

        if(omitProbabilities) {
            double[] innerPrediction = predictionMap.getOrDefault(doubleVectorialObservation, defaultPrediction);
            double[] outerPrediction = new double[innerPrediction.length];
            outerPrediction[0] = innerPrediction[0];
            outerPrediction[1] = innerPrediction[1];
            for (int i = 0; i < actionCount; i++) {
                outerPrediction[2 + i] = 1.0 / actionCount;
            }
            return outerPrediction;
        } else {
            return predictionMap.getOrDefault(doubleVectorialObservation, defaultPrediction);
        }
    }
}
