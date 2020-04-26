package vahy.vizualiation;

import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.List;

public class DataSeriesCollector {

    private final String dataTitle;
    private final List<ImmutableTuple<Double, List<Double>>> data = new ArrayList<>();

    public DataSeriesCollector(String dataTitle) {
        this.dataTitle = dataTitle;
    }

    public String getDataTitle() {
        return dataTitle;
    }

    public void addDataEntry(ImmutableTuple<Double, List<Double>> entry) {
        data.add(entry);
    }

    public List<ImmutableTuple<Double, List<Double>>> getData() {
        return data;
    }

    public ImmutableTuple<Double, List<Double>> getLatest() {
        return data.get(data.size() - 1);
    }

}
