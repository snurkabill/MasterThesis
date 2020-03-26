package vahy.vizualiation;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MyShittyFrameVisualization  extends ApplicationFrame {

    private final List<String> title;
    private final String xAxisLabel;
    private final String yAxisLabel;
    private final int width;
    private final int heigth;
    private final Color color;
    private final List<ChartPanel> chartPanelList = new ArrayList<>();

    public MyShittyFrameVisualization(String windowTitle, List<String> titleList, String xAxisLabelList, String yAxisLabelList, Color color) {
        this(windowTitle, titleList, xAxisLabelList, yAxisLabelList, 400, 280, color);
    }

    public MyShittyFrameVisualization(String windowTitle, List<String> titleList, String xAxisLabelList, String yAxisLabelList, int width, int height, Color color) {
        super(windowTitle);
        this.title = titleList;
        this.xAxisLabel = xAxisLabelList;
        this.yAxisLabel = yAxisLabelList;
        this.width = width;
        this.heigth = height;
        this.color = color;
        setLayout(new GridLayout(0, 3));
    }

    private JFreeChart createChart(final XYDataset dataset, int index) {
        final NumberAxis xAxis = new NumberAxis(xAxisLabel);
        final NumberAxis yAxis = new NumberAxis(yAxisLabel);
        final XYPlot plot = new XYPlot(dataset, xAxis, yAxis, new XYLineAndShapeRenderer(true, true));
        plot.setBackgroundPaint(Color.DARK_GRAY);
        plot.setOutlinePaint(Color.DARK_GRAY);
        plot.getRenderer(0).setSeriesPaint(0, color);
        return new JFreeChart(title.get(index), JFreeChart.DEFAULT_TITLE_FONT, plot, true);
    }

    public void draw(List<XYDataset> datasetList) {
        if(this.chartPanelList.isEmpty()) {
            for (int i = 0; i < datasetList.size(); i++) {
                final JFreeChart chart = createChart(datasetList.get(i), i);
                final ChartPanel chartPanel = new ChartPanel(chart);
                chartPanel.setChart(chart);
                chartPanel.setPreferredSize(new java.awt.Dimension(width, heigth));
                getContentPane().add(chartPanel);
                chartPanelList.add(chartPanel);
            }
        }
        for (int i = 0; i < datasetList.size(); i++) {
            var chartPanel = chartPanelList.get(i);
            chartPanel.getChart().getXYPlot().setDataset(datasetList.get(i));
        }

        this.pack();
        if(!this.isVisible()) {
            this.setVisible(true);
        }
        this.repaint();
    }
}
