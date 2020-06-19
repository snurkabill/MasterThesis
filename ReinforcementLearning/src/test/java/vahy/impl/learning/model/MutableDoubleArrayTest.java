package vahy.impl.learning.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class MutableDoubleArrayTest {

    private static final double DOUBLE_TOLERANCE = Math.pow(10, -10);

    @Test
    public void initializeTest() {

        var mda = new MutableDoubleArray(new double[] {0.0, 0.0, 0.0}, true);
        assertEquals(mda.getCounter(), 0);
        assertArrayEquals(mda.getDoubleArray(), new double[] {0.0, 0.0, 0.0});

        var mda2 = new MutableDoubleArray(new double[] {1.0, 2.0, 3.0}, false);
        assertEquals(mda2.getCounter(), 1);
        assertArrayEquals(mda2.getDoubleArray(), new double[] {1.0, 2.0, 3.0});
    }

    @Test
    public void addInstanceTest() {
        var mda = new MutableDoubleArray(new double[] {0.0, 0.0, 0.0, 0.0}, true);
        mda.addDataSample(new double[] {0.1, 0.2, 0.3, 0.4});

        assertEquals(mda.getCounter(), 1);
        assertArrayEquals(mda.getDoubleArray(), new double[] {0.1, 0.2, 0.3, 0.4});

        mda.addDataSample(new double[] {-0.1, -0.2, -0.3, -0.4});

        assertEquals(mda.getCounter(), 2);
        assertArrayEquals(mda.getDoubleArray(), new double[] {0.0, 0.0, 0.0, 0.0});

    }

}
