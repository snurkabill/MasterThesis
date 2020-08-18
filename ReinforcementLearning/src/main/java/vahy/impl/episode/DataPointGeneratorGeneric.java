package vahy.impl.episode;

import vahy.vizualization.DataPointGenerator;
import vahy.vizualization.DataSample;

import java.util.List;
import java.util.function.Function;

public class DataPointGeneratorGeneric<TDataSource> implements DataPointGenerator {

    private final String dataTitle;
    private final Function<TDataSource, List<Double>> function;

    private int counter = 0;
    private List<Double> valueList = null;

    public DataPointGeneratorGeneric(String dataTitle, Function<TDataSource, List<Double>> function) {
        this.dataTitle = dataTitle;
        this.function = function;
    }

    @Override
    public String getDataTitle() {
        return dataTitle;
    }

    @Override
    public DataSample get() {
        return new DataSample((double) counter, valueList);
    }

    public void addNewValue(TDataSource dataSource) {
        counter++;
        valueList = function.apply(dataSource);
    }

    public DataPointGeneratorGeneric<TDataSource> createCopy() {
        return new DataPointGeneratorGeneric<>(dataTitle, function);
    }
}
