package vahy;

import vahy.api.episode.InitialStateSupplier;
import vahy.api.learning.AbstractMonteCarloTrainer;
import vahy.api.learning.model.AbstractTrainableStateEvaluatingPolicySupplier;
import vahy.api.learning.model.TrainablePolicySupplier;
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
import vahy.impl.model.reward.DoubleScalarRewardDouble;
import vahy.impl.policy.AbstractTreeSearchPolicy;
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

    public static PolicySupplier<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> trainPolicy(InitialStateSupplier<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> initialStateSupplier,
                                                                                                               PolicySupplier<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> opponentPolicySupplier,
                                                                                                               RewardFactory<DoubleScalarRewardDouble> rewardFactory,
                                                                                                               RewardAggregator<DoubleScalarRewardDouble> rewardAggregator,
                                                                                                               double discountFactor,
                                                                                                               int observatonVectorSize,
                                                                                                               int rewardVectorSize,
                                                                                                               double learningRate,
                                                                                                               SplittableRandom random,
                                                                                                               int updateTreeCount) {
        TrainableRewardApproximator<DoubleScalarRewardDouble, DoubleVectorialObservation> trainableRewardApproximator = new TrainableRewardApproximatorImpl<>(
            new LinearModelNaiveImpl(observatonVectorSize, rewardVectorSize, learningRate),
            rewardFactory
        );

        TrainablePolicySupplier<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> trainablePolicySupplier = new TrainablePolicySupplier<>() {

            private final AbstractTrainableStateEvaluatingPolicySupplier<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> trainableStateEvaluatingPolicySupplier = new AbstractTrainableStateEvaluatingPolicySupplier<>(trainableRewardApproximator) {

                @Override
                public void train(List<ImmutableTuple<DoubleVectorialObservation, DoubleScalarRewardDouble>> episodeData) {
                    getTrainableRewardApproximator().train(episodeData);
                }

                @Override
                public Policy<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> initializePolicy(State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> initialState) {

                    double dummyConstant = 1.0;

                    SearchNodeFactory<
                        ActionType,
                        DoubleScalarRewardDouble,
                        DoubleVectorialObservation,
                        Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
                        Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>,
                        State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> searchNodeFactory = new SearchNodeBaseFactoryImpl<>(
                        (stateRewardReturn, parent) -> {
                            Double cumulativeReward = parent != null ? parent.getSearchNodeMetadata().getCumulativeReward().getValue() : 0.0;
                            return new Ucb1SearchNodeMetadata<>(new DoubleScalarRewardDouble(stateRewardReturn.getReward().getValue() + cumulativeReward), new LinkedHashMap<>());
                        });

                    SearchNode<
                        ActionType,
                        DoubleScalarRewardDouble,
                        DoubleVectorialObservation,
                        Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
                        Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>,
                        State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> root = searchNodeFactory.createNode(new ImmutableStateRewardReturnTuple<>(initialState, new DoubleScalarRewardDouble(0.0)), null, null);

                    NodeTransitionUpdater<
                        ActionType,
                        DoubleScalarRewardDouble,
                        DoubleVectorialObservation,
                        Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
                        Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>,
                        State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> transitionUpdater =
                            new Ucb1WithGivenProbabilitiesTransitionUpdater(discountFactor, rewardAggregator);

                    BaseNodeExpander<
                        ActionType,
                        DoubleScalarRewardDouble,
                        DoubleVectorialObservation,
                        Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
                        Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>> nodeExpander =
                            new BaseNodeExpander<>(searchNodeFactory, x -> new Ucb1StateActionMetadata<>(x.getReward()));

                    SearchTreeImpl<
                        ActionType,
                        DoubleScalarRewardDouble,
                        DoubleVectorialObservation,
                        Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
                        Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>,
                        State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>> searchTree = new SearchTreeImpl<
                            ActionType,
                            DoubleScalarRewardDouble,
                            DoubleVectorialObservation,
                            Ucb1StateActionMetadata<DoubleScalarRewardDouble>,
                            Ucb1SearchNodeMetadata<ActionType, DoubleScalarRewardDouble>,
                            State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation>>(
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
            };

            @Override
            public void train(List<ImmutableTuple<DoubleVectorialObservation, DoubleScalarRewardDouble>> episodeData) {
                this.trainableStateEvaluatingPolicySupplier.train(episodeData);
            }

            @Override
            public Policy<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> initializePolicy(State<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> initialState) {
                return this.trainableStateEvaluatingPolicySupplier.initializePolicy(initialState);
            }
        };

        AbstractMonteCarloTrainer<ActionType, DoubleScalarRewardDouble, DoubleVectorialObservation> trainer = new FirstVisitMontecarloTrainer<>(
            initialStateSupplier,
            trainablePolicySupplier,
            opponentPolicySupplier,
            rewardAggregator,
            discountFactor
        );
        for (int i = 0; i < 10; i++) {
            trainer.trainPolicy(() -> 1);
        }
        return trainablePolicySupplier;
    }
}
