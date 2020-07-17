package vahy.impl.learning.dataAggregator;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import vahy.impl.learning.model.MutableDoubleArray;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.LinkedList;

public class ReplayBufferDataAggregatorTest {

    @Test
    public void basicTest() {
        var replayBuffer = new ReplayBufferDataAggregator(2, new LinkedList<>());

        var data = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>();
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 0, 1}), new MutableDoubleArray(new double[] {1, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 1, 0}), new MutableDoubleArray(new double[] {2, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {1, 0, 0}), new MutableDoubleArray(new double[] {3, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 0, 1}), new MutableDoubleArray(new double[] {4, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 1, 0}), new MutableDoubleArray(new double[] {5, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {1, 0, 0}), new MutableDoubleArray(new double[] {6, 0}, false)));

        replayBuffer.addEpisodeSamples(data);

        var trainingDataset = replayBuffer.getTrainingDataset();
        var inputs = trainingDataset.getFirst();
        var targets = trainingDataset.getSecond();

        assertEquals(inputs.length, 6);
        assertEquals(targets.length, 6);

        for (int i = 0; i < inputs.length; i++) {
            assertArrayEquals(inputs[i].getObservedVector(), data.get(i).getFirst().getObservedVector());
            assertArrayEquals(targets[i], data.get(i).getSecond().getDoubleArray());
        }

    }

    @Test
    public void twoAdditionTest() {
        var replayBuffer = new ReplayBufferDataAggregator(1, new LinkedList<>());

        var data = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>();
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 0, 1}), new MutableDoubleArray(new double[] {1, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 1, 0}), new MutableDoubleArray(new double[] {2, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {1, 0, 0}), new MutableDoubleArray(new double[] {3, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 0, 1}), new MutableDoubleArray(new double[] {4, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 1, 0}), new MutableDoubleArray(new double[] {5, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {1, 0, 0}), new MutableDoubleArray(new double[] {6, 0}, false)));

        replayBuffer.addEpisodeSamples(data);

        var data2 = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>();
        data2.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 0, 1}), new MutableDoubleArray(new double[] {1, 0}, false)));
        data2.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 1, 0}), new MutableDoubleArray(new double[] {2, 0}, false)));
        data2.add(new ImmutableTuple<>(new DoubleVector(new double[] {1, 0, 0}), new MutableDoubleArray(new double[] {3, 0}, false)));
        data2.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 0, 1}), new MutableDoubleArray(new double[] {4, 0}, false)));
        data2.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 1, 0}), new MutableDoubleArray(new double[] {5, 0}, false)));
        data2.add(new ImmutableTuple<>(new DoubleVector(new double[] {1, 0, 0}), new MutableDoubleArray(new double[] {6, 0}, false)));

        replayBuffer.addEpisodeSamples(data2);

        var trainingDataset = replayBuffer.getTrainingDataset();
        var inputs = trainingDataset.getFirst();
        var targets = trainingDataset.getSecond();

        assertEquals(inputs.length, 6);
        assertEquals(targets.length, 6);

        for (int i = 0; i < inputs.length; i++) {
            assertArrayEquals(inputs[i].getObservedVector(), data.get(i).getFirst().getObservedVector());
            assertArrayEquals(targets[i], data.get(i).getSecond().getDoubleArray());
        }

    }

    @Test
    public void multiAdditionTest() {
        var replayBuffer = new ReplayBufferDataAggregator(2, new LinkedList<>());

        var data = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>();
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 0, 1}), new MutableDoubleArray(new double[] {1, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 1, 0}), new MutableDoubleArray(new double[] {2, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {1, 0, 0}), new MutableDoubleArray(new double[] {3, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 0, 1}), new MutableDoubleArray(new double[] {4, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 1, 0}), new MutableDoubleArray(new double[] {5, 0}, false)));
        data.add(new ImmutableTuple<>(new DoubleVector(new double[] {1, 0, 0}), new MutableDoubleArray(new double[] {6, 0}, false)));

        replayBuffer.addEpisodeSamples(data);

        var data2 = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>();
        data2.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 0, 1}), new MutableDoubleArray(new double[] {1, 0}, false)));
        data2.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 1, 0}), new MutableDoubleArray(new double[] {2, 0}, false)));
        data2.add(new ImmutableTuple<>(new DoubleVector(new double[] {1, 0, 0}), new MutableDoubleArray(new double[] {3, 0}, false)));
        data2.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 0, 1}), new MutableDoubleArray(new double[] {4, 0}, false)));
        data2.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 1, 0}), new MutableDoubleArray(new double[] {5, 0}, false)));
        data2.add(new ImmutableTuple<>(new DoubleVector(new double[] {1, 0, 0}), new MutableDoubleArray(new double[] {6, 0}, false)));

        replayBuffer.addEpisodeSamples(data2);

        var data3 = new ArrayList<ImmutableTuple<DoubleVector, MutableDoubleArray>>();
        data3.add(new ImmutableTuple<>(new DoubleVector(new double[] {1, 0, 1}), new MutableDoubleArray(new double[] {10, 0}, false)));
        data3.add(new ImmutableTuple<>(new DoubleVector(new double[] {0, 1, 1}), new MutableDoubleArray(new double[] {20, 0}, false)));
        data3.add(new ImmutableTuple<>(new DoubleVector(new double[] {1, 1, 0}), new MutableDoubleArray(new double[] {30, 0}, false)));

        replayBuffer.addEpisodeSamples(data3);

        var trainingDataset = replayBuffer.getTrainingDataset();
        var inputs = trainingDataset.getFirst();
        var targets = trainingDataset.getSecond();

        assertEquals(inputs.length, 9);
        assertEquals(targets.length, 9);

        for (int i = 0; i < data2.size(); i++) {
            assertArrayEquals(inputs[i].getObservedVector(), data2.get(i).getFirst().getObservedVector());
            assertArrayEquals(targets[i], data2.get(i).getSecond().getDoubleArray());
        }

        for (int i = data2.size(); i < data2.size() + data3.size(); i++) {
            assertArrayEquals(inputs[i].getObservedVector(), data3.get(i - data2.size()).getFirst().getObservedVector());
            assertArrayEquals(targets[i], data3.get(i - data2.size()).getSecond().getDoubleArray());
        }

    }

}
