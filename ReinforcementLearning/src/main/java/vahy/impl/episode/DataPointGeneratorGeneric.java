package vahy.impl.episode;

import vahy.utils.ImmutableTuple;
import vahy.vizualiation.DataPointGenerator;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataPointGeneratorGeneric<TDataSource> implements DataPointGenerator {

    private final String dataTitle;
    private final Function<TDataSource, Double> function;

    private int counter = 0;
    private List<Double> valueList = null;

    public DataPointGeneratorGeneric(String dataTitle, Function<TDataSource, Double> function) {
        this.dataTitle = dataTitle;
        this.function = function;
    }

    @Override
    public String getDataTitle() {
        return dataTitle;
    }

    @Override
    public ImmutableTuple<Double, List<Double>> get() {
        return new ImmutableTuple<>((double) counter, valueList);
    }

    public void addNewValue(List<TDataSource> dataSource) {
        counter++;
        valueList = dataSource.stream().map(function).collect(Collectors.toList());
    }

    public void addNewValue(TDataSource dataSource) {
        counter++;
        valueList = List.of(function.apply(dataSource));
    }

    public DataPointGeneratorGeneric<TDataSource> createCopy() {
        return new DataPointGeneratorGeneric<>(dataTitle, function);
    }
}
