package vahy.vizualiation;

import java.util.ArrayList;
import java.util.List;

public class DataSeriesCollector {

    private final String dataTitle;
    private final List<DataSample> data = new ArrayList<>();

    public DataSeriesCollector(String dataTitle) {
        this.dataTitle = dataTitle;
    }

    public String getDataTitle() {
        return dataTitle;
    }

    public void addDataEntry(DataSample sample) {
        data.add(sample);
    }

    public List<DataSample> getData() {
        return data;
    }

    public DataSample getLatest() {
        return data.get(data.size() - 1);
    }

}
