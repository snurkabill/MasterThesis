package vahy.vizualiation;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.Collections;
import java.util.List;

public class DataSeriesCreator {

    public static XYDataset createDataset(SeriesMetadata seriesMetadataList) {
        return createDataset(Collections.singletonList(seriesMetadataList));
    }

    public static XYDataset createDataset(List<SeriesMetadata> seriesMetadataList) {
        XYSeriesCollection collectionOfSeries = new XYSeriesCollection();
        for (SeriesMetadata seriesMetadata : seriesMetadataList) {
            XYSeries series = new XYSeries(seriesMetadata.getDataTitle());
            for (int i = 0; i < seriesMetadata.getData().size(); i++) {
                series.add(seriesMetadata.getData().get(i).getFirst(), seriesMetadata.getData().get(i).getSecond());
            }

            collectionOfSeries.addSeries(series);
        }
        return collectionOfSeries;
    }
}

