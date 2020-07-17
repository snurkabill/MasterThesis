package vahy.impl.learning.dataAggregator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class FirstVisitMonteCarloAggregatorTest {

    @Test
    public void basicTest() {
        FirstVisitMonteCarloDataAggregator dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var data = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>();
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 0, 1}), new MutableDoubleArray(new double[] {1, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 1, 0}), new MutableDoubleArray(new double[] {2, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {1, 0, 0}), new MutableDoubleArray(new double[] {3, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 0, 1}), new MutableDoubleArray(new double[] {4, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 1, 0}), new MutableDoubleArray(new double[] {5, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {1, 0, 0}), new MutableDoubleArray(new double[] {6, 0}, false)));
        dataAggregator.addEpisodeSamples(data);

        var trainingDataset = dataAggregator.getTrainingDataset();
        var inputs = trainingDataset.getFirst();
        var targets = trainingDataset.getSecond();

        assertEquals(inputs.length, 3);
        assertEquals(targets.length, 3);

        assertArrayEquals(targets[0], new double[] {1, 0});
        assertArrayEquals(targets[1], new double[] {2, 0});
        assertArrayEquals(targets[2], new double[] {3, 0});

        assertArrayEquals(inputs[0].getObservedVector(), new double[] {0, 0, 1});
        assertArrayEquals(inputs[1].getObservedVector(), new double[] {0, 1, 0});
        assertArrayEquals(inputs[2].getObservedVector(), new double[] {1, 0, 0});
    }

    @Test
    public void multipleAdditionsTest() {
        FirstVisitMonteCarloDataAggregator dataAggregator = new FirstVisitMonteCarloDataAggregator(new LinkedHashMap<>());

        var data = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>();
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 0, 1}), new MutableDoubleArray(new double[] {1, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 1, 0}), new MutableDoubleArray(new double[] {2, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {1, 0, 0}), new MutableDoubleArray(new double[] {3, 0}, false)));

        dataAggregator.addEpisodeSamples(data);

        var data2 = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>();
        data2.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 0, 1}), new MutableDoubleArray(new double[] {3, 2}, false)));
        data2.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 1, 0}), new MutableDoubleArray(new double[] {4, 4}, false)));
        data2.add(new ImmutableTuple<>(new DoubleVector(new double[] {1, 0, 0}), new MutableDoubleArray(new double[] {5, 6}, false)));

        dataAggregator.addEpisodeSamples(data2);

        var trainingDataset = dataAggregator.getTrainingDataset();
        var inputs = trainingDataset.getFirst();
        var targets = trainingDataset.getSecond();

        assertEquals(inputs.length, 3);
        assertEquals(targets.length, 3);

        assertArrayEquals(targets[0], new double[] {2, 1});
        assertArrayEquals(targets[1], new double[] {3, 2});
        assertArrayEquals(targets[2], new double[] {4, 3});

        assertArrayEquals(inputs[0].getObservedVector(), new double[] {0, 0, 1});
        assertArrayEquals(inputs[1].getObservedVector(), new double[] {0, 1, 0});
        assertArrayEquals(inputs[2].getObservedVector(), new double[] {1, 0, 0});

    }
}
