package vahy.vizualiation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProgressTracker {

    private static final Logger logger = LoggerFactory.getLogger(ProgressTracker.class.getName());

    private final List<DataPointGenerator> dataPointGeneratorList = new ArrayList<>();
    private final List<DataSeriesCollector> dataSeriesCollectorList = new ArrayList<>();
    private final ProgressTrackerSettings progressTrackerSettings;
    private MyShittyFrameVisualization visualization;
    private final Color color;
    private final String windowTitle;
    private boolean isFinalized = false;

    public ProgressTracker(ProgressTrackerSettings progressTrackerSettings, String windowTitle, Color color) {
        this.progressTrackerSettings = progressTrackerSettings;
        this.color = color;
        this.windowTitle = windowTitle;
    }

    public void registerDataCollector(DataPointGenerator dataPointGenerator) {
        dataPointGeneratorList.add(dataPointGenerator);
        dataSeriesCollectorList.add(new DataSeriesCollector(dataPointGenerator.getDataTitle()));
    }

    public void finalizeRegistration() {
        if(progressTrackerSettings.isDrawOnEnd() || progressTrackerSettings.isDrawOnNextLog()) {
            visualization = new MyShittyFrameVisualization(
                windowTitle,
                dataPointGeneratorList
                    .stream()
                    .map(DataPointGenerator::getDataTitle)
                    .collect(Collectors.toList()),
                "Iteration",
                "Value",
                color);
        }
        isFinalized = true;
    }

    private void gatherData() {
        for (int i = 0; i < dataPointGeneratorList.size(); i++) {
            dataSeriesCollectorList.get(i).addDataEntry(dataPointGeneratorList.get(i).get());
        }
    }

    public List<DataSeriesCollector> getCollectorList() {
        return this.dataSeriesCollectorList;
    }

    public void onNextLog() {
        if(!isFinalized) {
            throw new IllegalStateException("Visualization window was not finalized");
        }
        gatherData();

        if(dataSeriesCollectorList.stream().anyMatch(dataSeriesCollector -> dataSeriesCollector.getData().stream().anyMatch(x -> x.getSecond() == null))) {
            return;
        }

        if(progressTrackerSettings.isPrintOnNextLog()) {
            var stringBuilder = new StringBuilder();
            stringBuilder.append(System.lineSeparator());
            stringBuilder.append("Iteration: [");
            stringBuilder.append(dataSeriesCollectorList.get(0).getLatest().getFirst());
            stringBuilder.append("] ").append(System.lineSeparator());
            for (DataSeriesCollector dataSeriesCollector : dataSeriesCollectorList) {
                stringBuilder.append(dataSeriesCollector.getDataTitle());
                stringBuilder.append(" [");
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
        if(!isFinalized) {
            throw new IllegalStateException("Visualization window was not finalized");
        }
        if(progressTrackerSettings.isPrintOnEnd()) {
            logger.error("log printing on end not implemented");
        }
        if(progressTrackerSettings.isDrawOnEnd()) {
            visualization.draw(dataSeriesCollectorList.stream().map(XYDatasetBuilder::createDataset).collect(Collectors.toList()));
        }
    }
}
