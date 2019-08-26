package vahy.vizualiation;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.ApplicationFrame;

import java.awt.*;

public class MyShittyFrameVisualization  extends ApplicationFrame {

    private final String title;
    private final String xAxisLabel;
    private final String yAxisLabel;
    private final int width;
    private final int heigth;
    private final ChartPanel chartPanel = new ChartPanel(null);

    public MyShittyFrameVisualization(String title, String xAxisLabel, String yAxisLabel) {
        this(title, xAxisLabel, yAxisLabel, 560, 370);
    }

    public MyShittyFrameVisualization(String title, String xAxisLabel, String yAxisLabel, int width, int height) {
        super("Viz window");
        this.title = title;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.width = width;
        this.heigth = height;
    }

    private JFreeChart createChart(final XYDataset dataset) {
        final NumberAxis xAxis = new NumberAxis(xAxisLabel);
        final NumberAxis yAxis = new NumberAxis(yAxisLabel);
        final XYPlot plot = new XYPlot(dataset, xAxis, yAxis, new XYLineAndShapeRenderer(true, true));
        plot.setBackgroundPaint(Color.DARK_GRAY);
        plot.setOutlinePaint(Color.DARK_GRAY);
        final JFreeChart chart = new JFreeChart(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        return chart;
    }

    public void draw(XYDataset dataset) {
        final JFreeChart chart = createChart(dataset);
//        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setChart(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(width, heigth));
        chartPanel.setMouseZoomable(true, false);
        setContentPane(chartPanel);

        this.pack();
        if(!this.isVisible()) {
            this.setVisible(true);
        }
        // RefineryUtilities.positionFrameOnScreen(this, 0.1, 0.1);

//        this.update(this.getGraphics());
        this.repaint();

   }

}
