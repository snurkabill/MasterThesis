package vahy;

import vahy.api.episode.InitialStateSupplier;
import vahy.api.learning.AbstractMonteCarloTrainer;
import vahy.api.learning.model.AbstractTrainableStateEvaluatingPolicySupplier;
import vahy.api.learning.model.TrainableRewardApproximator;
import vahy.api.model.State;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.model.reward.RewardFactory;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicySupplier;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.factory.SearchNodeFactory;
import vahy.api.search.update.NodeTransitionUpdater;
import vahy.environment.ActionType;
import vahy.impl.learning.FirstVisitMontecarloTrainer;
import vahy.impl.learning.model.LinearModelNaiveImpl;
import vahy.impl.learning.model.TrainableRewardApproximatorImpl;
import vahy.impl.model.ImmutableStateRewardReturnTuple;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.policy.AbstractTreeSearchPolicy;
import vahy.impl.policy.random.UniformRandomWalkPolicy;
import vahy.impl.search.node.factory.SearchNodeBaseFactoryImpl;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1SearchNodeMetadata;
import vahy.impl.search.node.nodeMetadata.ucb1.Ucb1StateActionMetadata;
import vahy.impl.search.nodeExpander.BaseNodeExpander;
import vahy.impl.search.nodeSelector.treeTraversing.ucb1.Ucb1MinMaxExplorationConstantNodeSelector;
import vahy.impl.search.simulation.StateApproximatorSimulator;
import vahy.impl.search.tree.SearchTreeImpl;
import vahy.impl.search.update.TraversingTreeUpdater;
import vahy.search.Ucb1WithGivenProbabilitiesTransitionUpdater;
import vahy.utils.ImmutableTuple;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.SplittableRandom;

public class TrainedPolicyPrototype {

    public static PolicySupplier<ActionType, DoubleScalarReward, DoubleVectorialObservation> trainPolicy(InitialStateSupplier<ActionType, DoubleScalarReward, DoubleVectorialObservation> initialStateSupplier,
                                                                                                         PolicySupplier<ActionType, DoubleScalarReward, DoubleVectorialObservation> opponentPolicySupplier,
                                                                                                         RewardFactory<DoubleScalarReward> rewardFactory,
                                                                                                         RewardAggregator<DoubleScalarReward> rewardAggregator,
                                                                                                         double discountFactor,
                                                                                                         int observatonVectorSize,
                                                                                                         int rewardVectorSize,
                                                                                                         double learningRate,
                                                                                                         SplittableRandom random,
                                                                                                         int updateTreeCount,
                                                                                                         double explorationConstant) {
        TrainableRewardApproximator<DoubleScalarReward, DoubleVectorialObservation> trainableRewardApproximator = new TrainableRewardApproximatorImpl<>(
            new LinearModelNaiveImpl(observatonVectorSize, rewardVectorSize, learningRate),
            rewardFactory
        );

        AbstractTrainableStateEvaluatingPolicySupplier<ActionType, DoubleScalarReward, DoubleVectorialObservation> trainablePolicySupplier = new AbstractTrainableStateEvaluatingPolicySupplier<>(trainableRewardApproximator) {

            private Policy<ActionType, DoubleScalarReward, DoubleVectorialObservation> createPolicy(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> initialState) {
                double dummyConstant = 1.0;

                SearchNodeFactory<
                    ActionType,
                    DoubleScalarReward,
                    DoubleVectorialObservation,
                    Ucb1StateActionMetadata<DoubleScalarReward>,
                    Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>,
                    State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> searchNodeFactory = new SearchNodeBaseFactoryImpl<>(
                    (stateRewardReturn, parent) -> {
                        Double cumulativeReward = parent != null ? parent.getSearchNodeMetadata().getCumulativeReward().getValue() : 0.0;
                        return new Ucb1SearchNodeMetadata<>(new DoubleScalarReward(stateRewardReturn.getReward().getValue() + cumulativeReward), new LinkedHashMap<>());
                    });

                SearchNode<
                    ActionType,
                    DoubleScalarReward,
                    DoubleVectorialObservation,
                    Ucb1StateActionMetadata<DoubleScalarReward>,
                    Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>,
                    State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> root = searchNodeFactory.createNode(new ImmutableStateRewardReturnTuple<>(initialState, new DoubleScalarReward(0.0)), null, null);

                NodeTransitionUpdater<
                    ActionType,
                    DoubleScalarReward,
                    DoubleVectorialObservation,
                    Ucb1StateActionMetadata<DoubleScalarReward>,
                    Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>,
                    State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> transitionUpdater =
                    new Ucb1WithGivenProbabilitiesTransitionUpdater(discountFactor, rewardAggregator);

                BaseNodeExpander<
                    ActionType,
                    DoubleScalarReward,
                    DoubleVectorialObservation,
                    Ucb1StateActionMetadata<DoubleScalarReward>,
                    Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>> nodeExpander =
                    new BaseNodeExpander<>(searchNodeFactory, x -> new Ucb1StateActionMetadata<>(x.getReward()));

                SearchTreeImpl<
                    ActionType,
                    DoubleScalarReward,
                    DoubleVectorialObservation,
                    Ucb1StateActionMetadata<DoubleScalarReward>,
                    Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>,
                    State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> searchTree = new SearchTreeImpl<>(
                    root,
                    new Ucb1MinMaxExplorationConstantNodeSelector<>(random, dummyConstant),
                    nodeExpander,
                    new TraversingTreeUpdater<>(transitionUpdater),
                    new StateApproximatorSimulator<>(rewardAggregator, getTrainableRewardApproximator(), discountFactor)
                );
                return new AbstractTreeSearchPolicy<>(
                    random,
                    updateTreeCount,
                    searchTree
                ) { };
            }

            @Override
            public void train(List<ImmutableTuple<DoubleVectorialObservation, DoubleScalarReward>> episodeData) {
                getTrainableRewardApproximator().train(episodeData);
            }

            @Override
            public Policy<ActionType, DoubleScalarReward, DoubleVectorialObservation> initializePolicy(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> initialState) {
                return createPolicy(initialState);
            }

            @Override
            public Policy<ActionType, DoubleScalarReward, DoubleVectorialObservation> initializePolicyWithExploration(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> initialState) {
                return new Policy<>() {

                    private final Policy<ActionType, DoubleScalarReward, DoubleVectorialObservation> innerPolicy = createPolicy(initialState);
                    private final Policy<ActionType, DoubleScalarReward, DoubleVectorialObservation> randomPolicy = new UniformRandomWalkPolicy<>(random);

                    @Override
                    public double[] getActionProbabilityDistribution(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
                        return innerPolicy.getActionProbabilityDistribution(gameState);
                    }

                    @Override
                    public ActionType getDiscreteAction(State<ActionType, DoubleScalarReward, DoubleVectorialObservation> gameState) {
                        return random.nextDouble() < explorationConstant ?  randomPolicy.getDiscreteAction(gameState) : innerPolicy.getDiscreteAction(gameState);
                    }

                    @Override
                    public void updateStateOnOpponentActions(List<ActionType> opponentActionList) {
                        innerPolicy.updateStateOnOpponentActions(opponentActionList);
                    }
                };
            }
        };

        AbstractMonteCarloTrainer<ActionType, DoubleScalarReward, DoubleVectorialObservation> trainer = new FirstVisitMontecarloTrainer<>(
            initialStateSupplier,
            trainablePolicySupplier,
            opponentPolicySupplier,
            rewardAggregator,
            discountFactor
        );
        for (int i = 0; i < 20; i++) {
            trainer.trainPolicy(() -> 1);
        }
        return trainablePolicySupplier;
    }
}
