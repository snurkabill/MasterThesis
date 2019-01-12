package vahy.vizualiation;

import vahy.utils.ImmutableTuple;

import java.util.List;

public class SeriesMetadata {

    private final String dataTitle;
    private final List<ImmutableTuple<Double, Double>> data;

    public SeriesMetadata(String dataTitle, List<ImmutableTuple<Double, Double>> data) {
        this.dataTitle = dataTitle;
        this.data = data;
    }

    public String getDataTitle() {
        return dataTitle;
    }

    public List<ImmutableTuple<Double, Double>> getData() {
        return data;
    }

    public void addDataEntry(ImmutableTuple<Double, Double> entry) {
        data.add(entry);
    }
}
