package vahy.impl.learning.model;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.api.learning.model.SupervisedTrainableModel;
import vahy.api.model.reward.RewardFactory;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarRewardDouble;
import vahy.impl.model.reward.DoubleScalarRewardFactory;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NaiveLinearModelTest {

    private static final double LEARNING_TOLERANCE = Math.pow(10, -10);

    private List<ImmutableTuple<DoubleVectorialObservation, DoubleScalarRewardDouble>> getAndDatasetBase() {
        List<ImmutableTuple<DoubleVectorialObservation, DoubleScalarRewardDouble>> data = new ArrayList<>();
        data.add(new ImmutableTuple<>(new DoubleVectorialObservation(new double[] {0.0, 0.0}), new DoubleScalarRewardDouble(0.0)));
        data.add(new ImmutableTuple<>(new DoubleVectorialObservation(new double[] {1.0, 0.0}), new DoubleScalarRewardDouble(0.0)));
        data.add(new ImmutableTuple<>(new DoubleVectorialObservation(new double[] {0.0, 1.0}), new DoubleScalarRewardDouble(0.0)));
        data.add(new ImmutableTuple<>(new DoubleVectorialObservation(new double[] {1.0, 1.0}), new DoubleScalarRewardDouble(1.0)));
        return data;
    }

    private List<ImmutableTuple<DoubleVectorialObservation, DoubleScalarRewardDouble>> getOrDatasetBase() {
        List<ImmutableTuple<DoubleVectorialObservation, DoubleScalarRewardDouble>> data = new ArrayList<>();
        data.add(new ImmutableTuple<>(new DoubleVectorialObservation(new double[] {0.0, 0.0}), new DoubleScalarRewardDouble(0.0)));
        data.add(new ImmutableTuple<>(new DoubleVectorialObservation(new double[] {1.0, 0.0}), new DoubleScalarRewardDouble(1.0)));
        data.add(new ImmutableTuple<>(new DoubleVectorialObservation(new double[] {0.0, 1.0}), new DoubleScalarRewardDouble(1.0)));
        data.add(new ImmutableTuple<>(new DoubleVectorialObservation(new double[] {1.0, 1.0}), new DoubleScalarRewardDouble(1.0)));
        return data;
    }

    private List<ImmutableTuple<DoubleVectorialObservation, DoubleScalarRewardDouble>> copyAndShuffleDataset(List<ImmutableTuple<DoubleVectorialObservation, DoubleScalarRewardDouble>> originData,
                                                                                                             int copyCount) {
        List<ImmutableTuple<DoubleVectorialObservation, DoubleScalarRewardDouble>> dataCopy = new ArrayList<>();
        for (int i = 0; i < copyCount; i++) {
            dataCopy.addAll(originData);
        }
        Collections.shuffle(dataCopy);
        return dataCopy;
    }

    @Test
    public void fitAndTest() {
        runFit(copyAndShuffleDataset(getAndDatasetBase(), 100));
    }

    @Test
    public void fitOrTest() {
        runFit(copyAndShuffleDataset(getOrDatasetBase(), 100));
    }

    private void runFit(List<ImmutableTuple<DoubleVectorialObservation, DoubleScalarRewardDouble>> data) {
        RewardFactory<DoubleScalarRewardDouble> rewardFactory = new DoubleScalarRewardFactory();
        SupervisedTrainableModel linearModel = new LinearModelNaiveImpl(2, 1, 0.1);
        ModelReinforcementAdapter<DoubleScalarRewardDouble> naiveLinearModel = new ModelReinforcementAdapter<DoubleScalarRewardDouble>(linearModel,  rewardFactory);
        boolean atLeastOneFail = false;
        for (ImmutableTuple<DoubleVectorialObservation, DoubleScalarRewardDouble> entry : data) {
            double predicted = naiveLinearModel.approximateReward(entry.getFirst()).getValue();
            double expected = entry.getSecond().getValue();
            double roundedPrediction = Math.round(predicted);
            atLeastOneFail = atLeastOneFail || (Math.abs(expected - roundedPrediction) > LEARNING_TOLERANCE);
        }
        Assert.assertTrue(atLeastOneFail);
        for (int i = 0; i < 100; i++) {
            naiveLinearModel.fit(data.stream().map(ImmutableTuple::getFirst).collect(Collectors.toList()), data.stream().map(ImmutableTuple::getSecond).collect(Collectors.toList()));
        }
        for (ImmutableTuple<DoubleVectorialObservation, DoubleScalarRewardDouble> entry : data) {
            double predicted = naiveLinearModel.approximateReward(entry.getFirst()).getValue();
            double expected = entry.getSecond().getValue();
            double roundedPrediction = Math.round(predicted);
            Assert.assertEquals(roundedPrediction, expected, LEARNING_TOLERANCE);
        }
    }

}
