package vahy.chart;

import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ChartBuilder {

    private static final int CHART_SIZE_X = 1280;
    private static final int CHART_SIZE_Y = 720;

    private static final Logger LOGGER = LoggerFactory.getLogger("ReportMaker");

    public static void chart(final File target, final List<List<Double>> timeSeries, final String chartName) {
        LOGGER.info("Preparing data for report");
        final XYSeriesCollection dataset = new XYSeriesCollection();
        for (int k = 0; k < timeSeries.size(); k++) {
            final XYSeries xy = new XYSeries(k + "th timeSeries");
            long distributedSum = 0;
            List<Double> sequence = timeSeries.get(k);
            for (int i = 0; i < sequence.size(); i++) {
                xy.add(i, sequence.get(i));
            }
            dataset.addSeries(xy);
        }
        LOGGER.info("Preparing axes");
        final ValueAxis xAxis = new NumberAxis();
        final ValueAxis yAxis = new NumberAxis();
        LOGGER.info("Preparing chart");

        XYItemRenderer renderer = new XYLineAndShapeRenderer(true, true);
        final XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(Color.DARK_GRAY);
        plot.setOutlinePaint(Color.DARK_GRAY);
        final JFreeChart chart = new JFreeChart(chartName, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        LOGGER.info("Drawing chart");
        try {
            if (target.exists()) {
                target.delete();
            }
            ChartUtils.saveChartAsPNG(target, chart, ChartBuilder.CHART_SIZE_X, ChartBuilder.CHART_SIZE_Y, null);
            ChartBuilder.LOGGER.info("Chart written as {}.", target.getAbsolutePath());
        } catch (final IOException e) {
            ChartBuilder.LOGGER.warn("Cannot write chart file.", e);
        }
    }

}
