package vahy.vizualization;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import vahy.utils.ImmutableTuple;

import java.util.List;

public class XYDatasetBuilder {

    private XYDatasetBuilder() {
    }

    public static XYSeriesCollection createDataset(DataSeriesCollector dataSeriesCollector) {
        XYSeriesCollection collectionOfSeries = new XYSeriesCollection();
        List<DataSample> data = dataSeriesCollector.getData();
        for (int i = 0; i < data.get(0).getyAxisValueList().size(); i++) {
            XYSeries series = new XYSeries(i);
            series.setDescription(data.get(0).getyAxisValueList().get(i).getName());
            for (DataSample sample : data) {
                series.add(sample.getxAxisValue(), sample.getyAxisValueList().get(i).getValue());
            }
            collectionOfSeries.addSeries(series);
        }
        return collectionOfSeries;
    }

    public static XYDataset createDataset(List<List<ImmutableTuple<Double, Double>>> dataSeriesList, List<String> titleList) {
        if(dataSeriesList.size() != titleList.size()) {
            throw new IllegalArgumentException("Different lengths in inputs. [" + dataSeriesList.size() + "] and [" + titleList.size() + "]");
        }
        XYSeriesCollection collectionOfSeries = new XYSeriesCollection();
        int index = 0;
        for (List<ImmutableTuple<Double, Double>> data : dataSeriesList) {
            XYSeries series = new XYSeries(titleList.get(index));
            for (ImmutableTuple<Double, Double> dataPoint : data) {
                series.add(dataPoint.getFirst(), dataPoint.getSecond());
            }
            index++;
            collectionOfSeries.addSeries(series);
        }
        return collectionOfSeries;
    }

    public static XYDataset createDatasetWithFixedX(List<List<Double>> dataSeriesList, List<String> titleList) {
        if(dataSeriesList.size() != titleList.size()) {
            throw new IllegalArgumentException("Different lengths in inputs. [" + dataSeriesList.size() + "] and [" + titleList.size() + "]");
        }
        XYSeriesCollection collectionOfSeries = new XYSeriesCollection();
        int index = 0;
        for (List<Double> data : dataSeriesList) {
            XYSeries series = new XYSeries(titleList.get(index));
            for (int i = 0; i < data.size(); i++) {
                series.add(i, data.get(i));
            }
            index++;
            collectionOfSeries.addSeries(series);
        }
        return collectionOfSeries;
    }

}

