package vahy.environment.agent.policy.smart;


//
//public class Ucb1Policy extends AbstractTreeSearchPolicy<Ucb1StateActionMetadata<DoubleScalarReward>, Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>> {
//
//    public Ucb1Policy(
//        SplittableRandom random,
//        int uprateTreeCount,
//        ImmutableStateImpl gameState,
//        NodeTransitionUpdater<
//            ActionType,
//            DoubleScalarReward,
//            DoubleVectorialObservation,
//            Ucb1StateActionMetadata<DoubleScalarReward>,
//            Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>,
//            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> nodeTransitionUpdater,
//        NodeEvaluationSimulator<
//            ActionType,
//            DoubleScalarReward,
//            DoubleVectorialObservation,
//            Ucb1StateActionMetadata<DoubleScalarReward>,
//            Ucb1SearchNodeMetadata<ActionType, DoubleScalarReward>,
//            State<ActionType, DoubleScalarReward, DoubleVectorialObservation>> rewardSimulator) {
//        super(random,
//            uprateTreeCount,
//            new SearchNodeBaseFactoryImpl<>(
//                (stateRewardReturn, parent) -> {
//                    Double cumulativeReward = parent != null ? parent.getSearchNodeMetadata().getCumulativeReward().getValue() : 0.0;
//                    return new Ucb1SearchNodeMetadata<>(new DoubleScalarReward(stateRewardReturn.getReward().getValue() + cumulativeReward), new LinkedHashMap<>());
//                }
//            ),
//            // () -> new Ucb1NodeSelector<>(random, 1.0),
//             () -> new Ucb1MinMaxExplorationConstantNodeSelector<>(random, 1.0),
//            stateRewardReturn -> new Ucb1StateActionMetadata<>(stateRewardReturn.getReward()), nodeTransitionUpdater,
//            gameState,
//            rewardSimulator);
//    }
//}
