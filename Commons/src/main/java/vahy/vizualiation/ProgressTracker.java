package vahy.vizualiation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProgressTracker {

    private static final Logger logger = LoggerFactory.getLogger(ProgressTracker.class.getName());

    private final List<DataPointGenerator> dataPointGeneratorList = new ArrayList<>();
    private final List<DataSeriesCollector> dataSeriesCollectorList = new ArrayList<>();
    private final ProgressTrackerSettings progressTrackerSettings;
    private MyShittyFrameVisualization visualization;

    public ProgressTracker(ProgressTrackerSettings progressTrackerSettings) {
        this.progressTrackerSettings = progressTrackerSettings;
    }

    public void registerDataCollector(DataPointGenerator dataPointGenerator) {
        dataPointGeneratorList.add(dataPointGenerator);
        dataSeriesCollectorList.add(new DataSeriesCollector(dataPointGenerator.getDataTitle()));
    }

    public void finalizeRegistration() {
        if(progressTrackerSettings.isDrawOnEnd() || progressTrackerSettings.isDrawOnNextLog()) {
            String[] iterationArr = new String[dataPointGeneratorList.size()];
            String[] valueArr = new String[dataPointGeneratorList.size()];
            Arrays.fill(iterationArr, "Iteration");
            Arrays.fill(valueArr, "Value");
            visualization = new MyShittyFrameVisualization(dataPointGeneratorList.stream().map(DataPointGenerator::getDataTitle).collect(Collectors.toList()), Arrays.asList(iterationArr), Arrays.asList(valueArr));
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
            visualization.draw(dataSeriesCollectorList.stream().map(XYDatasetBuilder::createDataset).collect(Collectors.toList()));
        }
    }

    public void finalLog() {
        if(progressTrackerSettings.isPrintOnEnd()) {
            logger.error("log printing on end not implemented");
        }
        if(progressTrackerSettings.isDrawOnEnd()) {
            visualization.draw(dataSeriesCollectorList.stream().map(XYDatasetBuilder::createDataset).collect(Collectors.toList()));
        }
    }
}
