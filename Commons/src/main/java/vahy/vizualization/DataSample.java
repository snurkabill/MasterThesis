package vahy.vizualization;

import java.util.List;

public class DataSample {

    private final Double xAxisValue;
    private final List<Double> yAxisValueList;

    public DataSample(Double xAxisValue, List<Double> yAxisValueList) {
        this.xAxisValue = xAxisValue;
        this.yAxisValueList = yAxisValueList;
    }

    public Double getxAxisValue() {
        return xAxisValue;
    }

    public List<Double> getyAxisValueList() {
        return yAxisValueList;
    }
}
