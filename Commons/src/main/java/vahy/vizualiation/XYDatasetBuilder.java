package vahy.vizualiation;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.Collections;
import java.util.List;

public class XYDatasetBuilder {

    public static XYDataset createDataset(DataSeriesCollector dataSeriesCollector) {
        return createDataset(Collections.singletonList(dataSeriesCollector));
    }

    public static XYDataset createDataset(List<DataSeriesCollector> dataSeriesCollector) {
        XYSeriesCollection collectionOfSeries = new XYSeriesCollection();
        for (DataSeriesCollector seriesMetadata : dataSeriesCollector) {
            XYSeries series = new XYSeries(seriesMetadata.getDataTitle());
            for (int i = 0; i < seriesMetadata.getData().size(); i++) {
                var data = seriesMetadata.getData().get(i);
                series.add(data.getFirst(), data.getSecond());
            }

            collectionOfSeries.addSeries(series);
        }
        return collectionOfSeries;
    }
}

