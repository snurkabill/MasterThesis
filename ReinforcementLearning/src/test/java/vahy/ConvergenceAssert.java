package vahy;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class ConvergenceAssert {

    public static final double TEST_CONVERGENCE_ASSERT_TOLERANCE = Math.pow(10, -10);
    public static final int TEST_THREAD_COUNT = Runtime.getRuntime().availableProcessors() - 1;

    public static void assertConvergenceResult(double expectedMin, double expectedMax, double actual, String propertyName) {
        if(expectedMin > expectedMax) {
            throw new IllegalStateException("Poorly written test. Max bound [ " + expectedMax + " ] must be above min bound [ " + expectedMin + " ].");
        }
        assertTrue(expectedMin <= actual, "Expected min " + propertyName + ": [" + expectedMin + "] but actual was: [" + actual + "]");
        assertTrue(expectedMax >= actual, "Expected max " + propertyName + ": [" + expectedMax + "] but actual was: [" + actual + "]");
    }

    public ConvergenceAssert() {}

}
