package vahy.impl.learning.model;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.api.learning.model.SupervisedTrainableModel;
import vahy.api.model.reward.RewardFactory;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.model.reward.DoubleScalarRewardFactory;
import vahy.utils.ImmutableTuple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NaiveLinearModelTest {

    private static final double LEARNING_TOLERANCE = Math.pow(10, -10);

    private List<ImmutableTuple<DoubleVectorialObservation, DoubleReward>> getAndDatasetBase() {
        List<ImmutableTuple<DoubleVectorialObservation, DoubleReward>> data = new ArrayList<>();
        data.add(new ImmutableTuple<>(new DoubleVectorialObservation(new double[] {0.0, 0.0}), new DoubleReward(0.0)));
        data.add(new ImmutableTuple<>(new DoubleVectorialObservation(new double[] {1.0, 0.0}), new DoubleReward(0.0)));
        data.add(new ImmutableTuple<>(new DoubleVectorialObservation(new double[] {0.0, 1.0}), new DoubleReward(0.0)));
        data.add(new ImmutableTuple<>(new DoubleVectorialObservation(new double[] {1.0, 1.0}), new DoubleReward(1.0)));
        return data;
    }

    private List<ImmutableTuple<DoubleVectorialObservation, DoubleReward>> getOrDatasetBase() {
        List<ImmutableTuple<DoubleVectorialObservation, DoubleReward>> data = new ArrayList<>();
        data.add(new ImmutableTuple<>(new DoubleVectorialObservation(new double[] {0.0, 0.0}), new DoubleReward(0.0)));
        data.add(new ImmutableTuple<>(new DoubleVectorialObservation(new double[] {1.0, 0.0}), new DoubleReward(1.0)));
        data.add(new ImmutableTuple<>(new DoubleVectorialObservation(new double[] {0.0, 1.0}), new DoubleReward(1.0)));
        data.add(new ImmutableTuple<>(new DoubleVectorialObservation(new double[] {1.0, 1.0}), new DoubleReward(1.0)));
        return data;
    }

    private List<ImmutableTuple<DoubleVectorialObservation, DoubleReward>> copyAndShuffleDataset(List<ImmutableTuple<DoubleVectorialObservation, DoubleReward>> originData,
                                                                                                 int copyCount) {
        List<ImmutableTuple<DoubleVectorialObservation, DoubleReward>> dataCopy = new ArrayList<>();
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

    private void runFit(List<ImmutableTuple<DoubleVectorialObservation, DoubleReward>> data) {
        RewardFactory<DoubleReward> rewardFactory = new DoubleScalarRewardFactory();
        SupervisedTrainableModel linearModel = new LinearModelNaiveImpl(2, 1, 0.1);
        ModelReinforcementAdapter<DoubleReward> naiveLinearModel = new ModelReinforcementAdapter<DoubleReward>(linearModel,  rewardFactory);
        boolean atLeastOneFail = false;
        for (ImmutableTuple<DoubleVectorialObservation, DoubleReward> entry : data) {
            double predicted = naiveLinearModel.approximateReward(entry.getFirst()).getValue();
            double expected = entry.getSecond().getValue();
            double roundedPrediction = Math.round(predicted);
            atLeastOneFail = atLeastOneFail || (Math.abs(expected - roundedPrediction) > LEARNING_TOLERANCE);
        }
        Assert.assertTrue(atLeastOneFail);
        for (int i = 0; i < 100; i++) {
            naiveLinearModel.fit(data.stream().map(ImmutableTuple::getFirst).collect(Collectors.toList()), data.stream().map(ImmutableTuple::getSecond).collect(Collectors.toList()));
        }
        for (ImmutableTuple<DoubleVectorialObservation, DoubleReward> entry : data) {
            double predicted = naiveLinearModel.approximateReward(entry.getFirst()).getValue();
            double expected = entry.getSecond().getValue();
            double roundedPrediction = Math.round(predicted);
            Assert.assertEquals(roundedPrediction, expected, LEARNING_TOLERANCE);
        }
    }

}
