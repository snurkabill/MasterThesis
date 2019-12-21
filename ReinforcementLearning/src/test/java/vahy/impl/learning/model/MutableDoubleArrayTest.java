package vahy.impl.learning.model;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MutableDoubleArrayTest {

    private static final double DOUBLE_TOLERANCE = Math.pow(10, -10);

    @Test
    public void initializeTest() {

        var mda = new MutableDoubleArray(new double[] {0.0, 0.0, 0.0}, true);
        Assert.assertEquals(mda.getCounter(), 0);
        Assert.assertEquals(mda.getDoubleArray(), new double[] {0.0, 0.0, 0.0});

        var mda2 = new MutableDoubleArray(new double[] {1.0, 2.0, 3.0}, false);
        Assert.assertEquals(mda2.getCounter(), 1);
        Assert.assertEquals(mda2.getDoubleArray(), new double[] {1.0, 2.0, 3.0});
    }

    @Test
    public void addInstanceTest() {
        var mda = new MutableDoubleArray(new double[] {0.0, 0.0, 0.0, 0.0}, true);
        mda.addDataSample(new double[] {0.1, 0.2, 0.3, 0.4});

        Assert.assertEquals(mda.getCounter(), 1);
        Assert.assertEquals(mda.getDoubleArray(), new double[] {0.1, 0.2, 0.3, 0.4});

        mda.addDataSample(new double[] {-0.1, -0.2, -0.3, -0.4});

        Assert.assertEquals(mda.getCounter(), 2);
        Assert.assertEquals(mda.getDoubleArray(), new double[] {0.0, 0.0, 0.0, 0.0});

    }

}
