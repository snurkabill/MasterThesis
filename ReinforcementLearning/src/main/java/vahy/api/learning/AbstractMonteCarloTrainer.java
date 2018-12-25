package vahy.api.learning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.episode.Episode;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.learning.model.TrainablePolicySupplier;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.model.reward.DoubleVectorialReward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.policy.PolicySupplier;
import vahy.impl.learning.model.RolloutGameSampler;
import vahy.impl.model.observation.DoubleVector;
import vahy.utils.ImmutableTuple;
import vahy.utils.MutableTuple;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractMonteCarloTrainer<
    TAction extends Action,
    TReward extends DoubleVectorialReward,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> extends AbstractTrainer { // TODO: make observation and reward more abstract

    private static final Logger logger = LoggerFactory.getLogger(AbstractMonteCarloTrainer.class.getName());

    private final RolloutGameSampler<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> rolloutGameSampler;
    private final TrainablePolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> trainablePolicySupplier;
    private final Map<TState, MutableTuple<Integer, TReward>> visitAverageRewardMap = new LinkedHashMap<>();
    protected final RewardAggregator<TReward> rewardAggregator;

    public AbstractMonteCarloTrainer(InitialStateSupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                                     TrainablePolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> trainablePolicySupplier,
                                     PolicySupplier<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> opponentPolicySupplier,
                                     RewardAggregator<TReward> rewardAggregator) {
        this.rewardAggregator = rewardAggregator;
        this.rolloutGameSampler = new RolloutGameSampler<>(initialStateSupplier, trainablePolicySupplier, opponentPolicySupplier);
        this.trainablePolicySupplier = trainablePolicySupplier;
    }

    @Override
    public void trainPolicy(int episodeCount) {
        logger.info("Starting MonteCarlo training on [{}] episodeCount", episodeCount);
        List<Episode<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> episodeHistoryList = rolloutGameSampler.sampleEpisodes(episodeCount);
        for (Episode<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> entry : episodeHistoryList) {
            addVisitedRewards(calculatedVisitedRewards(entry));
        }
        logger.debug("Sampled all episodes");
        List<ImmutableTuple<TPlayerObservation, TReward>> observationRewardList = new ArrayList<>();
        for (Map.Entry<TState, MutableTuple<Integer, TReward>> entry : visitAverageRewardMap.entrySet()) {
            observationRewardList.add(new ImmutableTuple<>(entry.getKey().getPlayerObservation(), entry.getValue().getSecond()));
        }
        trainablePolicySupplier.train(observationRewardList);
    }

    protected abstract Map<TState, TReward> calculatedVisitedRewards(Episode<TAction, TReward, TPlayerObservation, TOpponentObservation, TState> episode);

    protected void addVisitedRewards(Map<TState, TReward> sampledStateVisitMap) {
        for (Map.Entry<TState, TReward> entry : sampledStateVisitMap.entrySet()) {
            if(!entry.getKey().isOpponentTurn()) {
                if(visitAverageRewardMap.containsKey(entry.getKey())) {
                    MutableTuple<Integer, TReward> integerTRewardMutableTuple = visitAverageRewardMap.get(entry.getKey());
                    TReward averagedReward = rewardAggregator.averageReward(integerTRewardMutableTuple.getSecond(), integerTRewardMutableTuple.getFirst(), entry.getValue());
                    integerTRewardMutableTuple.setFirst(integerTRewardMutableTuple.getFirst() + 1);
                    integerTRewardMutableTuple.setSecond(averagedReward);
                } else {
                    visitAverageRewardMap.put(entry.getKey(), new MutableTuple<>(1, entry.getValue()));
                }
            } else {
                throw new IllegalStateException("Paranoia exception. Opponent states should have been already filtered out");
            }
        }

//        for (Map.Entry<State<TAction, TReward, TObservation>, MutableTuple<Integer, TReward>> entry1 : visitAverageRewardMap.entrySet()) {
//            for (Map.Entry<State<TAction, TReward, TObservation>, MutableTuple<Integer, TReward>> entry2 : visitAverageRewardMap.entrySet()) {
//                double[] observation1 = entry1.getKey().getPlayerObservation().getObservedVector();
//                double[] observation2 = entry2.getKey().getPlayerObservation().getObservedVector();
//                int hash1 = entry1.getKey().hashCode();
//                int hash2 = entry2.getKey().hashCode();
//                if(Arrays.equals(observation1, observation2)) {
//                    if(hash1 != hash2) {
//                        System.out.println("asdf");
//                        System.out.println(entry1.hashCode());
//                        System.out.println(entry2.hashCode());
//                    }
//
//                }
//            }
//        }

    }
}
