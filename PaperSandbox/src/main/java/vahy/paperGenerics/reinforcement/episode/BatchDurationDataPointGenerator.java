package vahy.paperGenerics.reinforcement.episode;

import vahy.utils.ImmutableTuple;
import vahy.vizualiation.DataPointGenerator;

public class BatchDurationDataPointGenerator implements DataPointGenerator {

    private int counter = 0;
    private double value = Double.NaN;

    @Override
    public String getDataTitle() {
        return "Normalized episode duration per batch [ms]";
    }

    @Override
    public ImmutableTuple<Double, Double> get() {
        return new ImmutableTuple<>((double) counter, value);
    }

    public void addNewValue(int batchId, double batchDuration) {
        counter = batchId;
        value = batchDuration;
    }

}
