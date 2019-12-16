package vahy.impl.predictor;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

public class DataTablePredictorTest {

    @Test
    public void defaultPredictionTest() {
        DataTablePredictor dataTablePredictor = new DataTablePredictor(new double[] {1, 2, 3});

        Assert.assertEquals(dataTablePredictor.apply(new DoubleVector(new double[]{-123})), new double[] {1, 2, 3});
        Assert.assertEquals(dataTablePredictor.apply(new DoubleVector(new double[]{-1, 2, 3})), new double[] {1, 2, 3});
        Assert.assertEquals(dataTablePredictor.apply(new DoubleVector(new double[]{123})), new double[] {1, 2, 3});
        Assert.assertEquals(dataTablePredictor.apply(new DoubleVector(new double[]{42, 84})), new double[] {1, 2, 3});
    }

    @Test
    public void knownPredictionsTest() {
        DataTablePredictor dataTablePredictor = new DataTablePredictor(new double[] {1, 1, 1});

        var doubleVectors = new DoubleVector[] {
            new DoubleVector(new double[] {1}),
            new DoubleVector(new double[] {2}),
            new DoubleVector(new double[] {3}),
            new DoubleVector(new double[] {4}),
        };
        var predictions = new double[][] {
            new double[] {0, 0, 1},
            new double[] {0, 1, 0},
            new double[] {0, 1, 1},
            new double[] {1, 0, 0},
        };
        var data = new ImmutableTuple<>(doubleVectors, predictions);
        dataTablePredictor.train(data);
        Assert.assertEquals(dataTablePredictor.apply(new DoubleVector(new double[]{1})), new double[] {0, 0, 1});
        Assert.assertEquals(dataTablePredictor.apply(new DoubleVector(new double[]{2})), new double[] {0, 1, 0});
        Assert.assertEquals(dataTablePredictor.apply(new DoubleVector(new double[]{3})), new double[] {0, 1, 1});
        Assert.assertEquals(dataTablePredictor.apply(new DoubleVector(new double[]{4})), new double[] {1, 0, 0});
        Assert.assertEquals(dataTablePredictor.apply(new DoubleVector(new double[]{0})), new double[] {1, 1, 1});
        Assert.assertEquals(dataTablePredictor.apply(new DoubleVector(new double[]{5})), new double[] {1, 1, 1});
    }


}
