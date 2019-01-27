package vahy.paperGenerics.reinforcement;

import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DataTableApproximator<TObservation extends DoubleVector> extends TrainableApproximator<TObservation> {

    public DataTableApproximator() {
        super(null);
    }

    private static double[] defaultPrediction = {0.0, 0.0, 1/3.0, 1/3.0, 1/3.0};
    private HashMap<TObservation, double[]> predictionMap = new HashMap<>();

    @Override
    public void train(List<ImmutableTuple<TObservation, double[]>> episodeData) {
        predictionMap = episodeData.stream().collect(Collectors.toMap(ImmutableTuple::getFirst, ImmutableTuple::getSecond, (oldValue, newValue) -> oldValue, HashMap::new));
    }

    @Override
    public double[] apply(TObservation doubleVectorialObservation) {
        if(predictionMap.containsKey(doubleVectorialObservation)) {

            double[] newArray = predictionMap.get(doubleVectorialObservation);

//            newArray[2] = 1.0/3.0;
//            newArray[3] = 1.0/3.0;
//            newArray[4] = 1.0/3.0;

            return newArray;
        } else {
            return defaultPrediction;
        }
    }
}