package vahy.impl.learning.model;

import vahy.api.learning.model.SupervisedTrainableModel;
import vahy.api.learning.model.TrainableRewardApproximator;
import vahy.api.model.reward.DoubleVectorialReward;
import vahy.api.model.reward.RewardFactory;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.utils.ImmutableTuple;

import java.util.Iterator;
import java.util.List;

public class TrainableRewardApproximatorImpl<TReward extends DoubleVectorialReward, TObservation extends DoubleVectorialObservation> implements TrainableRewardApproximator<TReward, TObservation> {

    // TODO: make this class more general not just using double arrays

    private final SupervisedTrainableModel supervisedTrainableModel;
    private final RewardFactory<TReward> rewardFactory;

    public TrainableRewardApproximatorImpl(SupervisedTrainableModel supervisedTrainableModel, RewardFactory<TReward> rewardFactory) {
        this.supervisedTrainableModel = supervisedTrainableModel;
        this.rewardFactory = rewardFactory;
    }

    @Override
    public TReward apply(TObservation observation) {
        return rewardFactory.fromNumericVector(supervisedTrainableModel.predict(observation.getObservedVector()));
    }

    @Override
    public void train(List<ImmutableTuple<TObservation, TReward>> episodeData) {
        double[][] input = new double[episodeData.size()][];
        double[][] target = new double[episodeData.size()][];
        Iterator<ImmutableTuple<TObservation, TReward>> iterator = episodeData.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            ImmutableTuple<TObservation, TReward> next = iterator.next();
            input[i] = next.getFirst().getObservedVector();
            target[i] = next.getSecond().getAsVector();
        }
        supervisedTrainableModel.fit(input, target);
    }
}
