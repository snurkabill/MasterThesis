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
    private final List<String> xAxisLabel;
    private final List<String> yAxisLabel;
    private final int width;
    private final int heigth;
    private final List<ChartPanel> chartPanelList = new ArrayList<>();

    public MyShittyFrameVisualization(List<String> titleList, List<String> xAxisLabelList, List<String> yAxisLabelList) {
        this(titleList, xAxisLabelList, yAxisLabelList, 400, 280);
    }

    public MyShittyFrameVisualization(List<String> titleList, List<String> xAxisLabelList, List<String> yAxisLabelList, int width, int height) {
        super("Viz window");
        if(titleList.size() != xAxisLabelList.size() || titleList.size() != yAxisLabelList.size()) {
            throw new IllegalArgumentException("Different list sizes of arguments");
        }
        this.title = titleList;
        this.xAxisLabel = xAxisLabelList;
        this.yAxisLabel = yAxisLabelList;
        this.width = width;
        this.heigth = height;
        setLayout(new GridLayout(0, 3));
    }

    private JFreeChart createChart(final XYDataset dataset, int index) {
        final NumberAxis xAxis = new NumberAxis(xAxisLabel.get(index));
        final NumberAxis yAxis = new NumberAxis(yAxisLabel.get(index));
        final XYPlot plot = new XYPlot(dataset, xAxis, yAxis, new XYLineAndShapeRenderer(true, true));
        plot.setBackgroundPaint(Color.DARK_GRAY);
        plot.setOutlinePaint(Color.DARK_GRAY);
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
