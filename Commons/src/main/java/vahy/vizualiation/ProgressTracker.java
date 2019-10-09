package vahy.vizualiation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ProgressTracker {

    private static final Logger logger = LoggerFactory.getLogger(ProgressTracker.class.getName());

    private final List<DataPointGenerator> dataPointGeneratorList = new ArrayList<>();
    private final List<DataSeriesCollector> dataSeriesCollectorList = new ArrayList<>();
    private final List<MyShittyFrameVisualization> visualizationList = new ArrayList<>();
    private final ProgressTrackerSettings progressTrackerSettings;

    public ProgressTracker(ProgressTrackerSettings progressTrackerSettings) {
        this.progressTrackerSettings = progressTrackerSettings;
    }

    public void registerDataCollector(DataPointGenerator dataPointGenerator) {
        dataPointGeneratorList.add(dataPointGenerator);
        dataSeriesCollectorList.add(new DataSeriesCollector(dataPointGenerator.getDataTitle()));
        if(progressTrackerSettings.isDrawOnEnd() || progressTrackerSettings.isDrawOnNextLog()) {
            visualizationList.add(new MyShittyFrameVisualization(dataPointGenerator.getDataTitle(), "Iteration", "Value"));
        }
    }

    private void gatherData() {
        for (int i = 0; i < dataPointGeneratorList.size(); i++) {
            dataSeriesCollectorList.get(i).addDataEntry(dataPointGeneratorList.get(i).get());
        }
    }

    public void onNextLog() {
        gatherData();

        if(progressTrackerSettings.isPrintOnNextLog()) {
            var stringBuilder = new StringBuilder();
            stringBuilder.append(System.lineSeparator());
            for (DataSeriesCollector dataSeriesCollector : dataSeriesCollectorList) {
                stringBuilder.append(dataSeriesCollector.getDataTitle());
                stringBuilder.append(" X: [");
                stringBuilder.append(dataSeriesCollector.getLatest().getFirst());
                stringBuilder.append("] ");
                stringBuilder.append("Y: [");
                stringBuilder.append(dataSeriesCollector.getLatest().getSecond());
                stringBuilder.append("]");
                stringBuilder.append(System.lineSeparator());
            }
            logger.info(stringBuilder.toString());
        }
        if(progressTrackerSettings.isDrawOnNextLog()) {
            for (int i = 0; i < dataSeriesCollectorList.size(); i++) {
                visualizationList.get(i).draw(XYDatasetBuilder.createDataset(dataSeriesCollectorList.get(i)));
            }
        }
    }

    public void finalLog() {
        if(progressTrackerSettings.isPrintOnEnd()) {
            logger.error("log printing on end not implemented");
        }
        if(progressTrackerSettings.isDrawOnEnd()) {
            for (int i = 0; i < dataSeriesCollectorList.size(); i++) {
                visualizationList.get(i).draw(XYDatasetBuilder.createDataset(dataSeriesCollectorList.get(i)));
            }
        }
    }
}
