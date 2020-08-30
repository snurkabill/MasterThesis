package vahy.vizualization;

import java.util.List;

public class DataSample {

    private final Double xAxisValue;
    private final List<LabelData> yAxisValueList;

    public DataSample(Double xAxisValue, List<LabelData> yAxisValueList) {
        this.xAxisValue = xAxisValue;
        this.yAxisValueList = yAxisValueList;
    }

    public Double getxAxisValue() {
        return xAxisValue;
    }

    public List<LabelData> getyAxisValueList() {
        return yAxisValueList;
    }
}
