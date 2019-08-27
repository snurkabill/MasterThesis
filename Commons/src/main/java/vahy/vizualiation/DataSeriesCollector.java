package vahy.vizualiation;

import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.List;

public class DataSeriesCollector {

    private final String dataTitle;
    private final List<ImmutableTuple<Double, Double>> data = new ArrayList<>();

    public DataSeriesCollector(String dataTitle) {
        this.dataTitle = dataTitle;
    }

    public String getDataTitle() {
        return dataTitle;
    }

    public void addDataEntry(ImmutableTuple<Double, Double> entry) {
        data.add(entry);
    }

    public List<ImmutableTuple<Double, Double>> getData() {
        return data;
    }

    public ImmutableTuple<Double, Double> getLatest() {
        return data.get(data.size() - 1);
    }

}
