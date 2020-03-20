package vahy.impl.episode;

import vahy.utils.ImmutableTuple;
import vahy.vizualiation.DataPointGenerator;

import java.util.function.Function;

public class DataPointGeneratorGeneric<TDataSource> implements DataPointGenerator {

    private final String dataTitle;
    private final Function<TDataSource, Double> function;

    private int counter = 0;
    private double value = Double.NaN;

    public DataPointGeneratorGeneric(
        String dataTitle,
        Function<TDataSource, Double> function)
    {
        this.dataTitle = dataTitle;
        this.function = function;
    }

    @Override
    public String getDataTitle() {
        return dataTitle;
    }

    @Override
    public ImmutableTuple<Double, Double> get() {
        return new ImmutableTuple<>((double) counter, value);
    }

    public void addNewValue(TDataSource dataSource) {
        counter++;
        value = function.apply(dataSource);
    }
}
