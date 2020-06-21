package vahy.impl.policy.mcts;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.search.nodeSelector.EnvironmentSamplingNodeSelector;

import java.util.SplittableRandom;

public class Ucb1NodeSelector<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>>
    extends EnvironmentSamplingNodeSelector<TAction, TObservation, MCTSMetadata, TState> {

    protected final double cpuctParameter;
    private final double[] valueArray;
    private final int[] indexArray;

    public Ucb1NodeSelector(SplittableRandom random, double cpuctParameter, int maxBranchingCount) {
        super(random);
        this.cpuctParameter = cpuctParameter;
        this.indexArray = new int[maxBranchingCount];
        this.valueArray = new double[maxBranchingCount];
    }

    @Override
    protected TAction getBestAction_inner(SearchNode<TAction, TObservation, MCTSMetadata, TState> node) {
        TAction[] possibleActions = node.getAllPossibleActions();
        var searchNodeMap = node.getChildNodeMap();
        var inGameEntityIdOnTurn = node.getStateWrapper().getInGameEntityOnTurnId();

        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < possibleActions.length; i++) {
            var metadata = searchNodeMap.get(possibleActions[i]).getSearchNodeMetadata();
            double value = metadata.getExpectedReward()[inGameEntityIdOnTurn] + metadata.getGainedReward()[inGameEntityIdOnTurn];
            if(max < value) {
                max = value;
            }
            if(min > value) {
                min = value;
            }
            valueArray[i] = value;
        }

        int maxIndex = -1;
        double maxValue = -Double.MAX_VALUE;

        int totalNodeVisitCount = node.getSearchNodeMetadata().getVisitCounter();
        int maxIndexCount = 0;
        if(max != min) {
            var norm = max - min;
            for (int i = 0; i < possibleActions.length; i++) {
                var metadata = searchNodeMap.get(possibleActions[i]).getSearchNodeMetadata();
                var vValue = (valueArray[i] - min) / norm;
                var uValue = cpuctParameter * Math.sqrt(totalNodeVisitCount / (1.0 + metadata.getVisitCounter()));
                var quValue = vValue + uValue;
//                logger.trace("Index: [{}], qValue[{}]", i, quValue);
                if(quValue > maxValue) {
                    maxIndex = i;
                    maxValue = quValue;
                    maxIndexCount = 0;
                } else if(quValue == maxValue) {
                    if(maxIndexCount == 0) {
                        indexArray[0] = maxIndex;
                        indexArray[1] = i;
                        maxIndexCount = 2;
                    } else {
                        indexArray[maxIndexCount] = i;
                        maxIndexCount++;
                    }
                }
            }
        } else {
            for (int i = 0; i < possibleActions.length; i++) {
                var metadata = searchNodeMap.get(possibleActions[i]).getSearchNodeMetadata();
                var quValue = cpuctParameter * Math.sqrt(totalNodeVisitCount / (1.0 + metadata.getVisitCounter()));
//                logger.trace("Index: [{}], qValue[{}]", i, quValue);
                if(quValue > maxValue) {
                    maxIndex = i;
                    maxValue = quValue;
                    maxIndexCount = 0;
                } else if(quValue == maxValue) {
                    if(maxIndexCount == 0) {
                        indexArray[0] = maxIndex;
                        indexArray[1] = i;
                        maxIndexCount = 2;
                    } else {
                        indexArray[maxIndexCount] = i;
                        maxIndexCount++;
                    }
                }
            }
        }
        if(maxIndexCount == 0) {
            return possibleActions[maxIndex];
        } else {
            return possibleActions[indexArray[random.nextInt(maxIndexCount)]];
        }
    }

    @Override
    public SearchNode<TAction, TObservation, MCTSMetadata, TState> selectNextNode(SearchNode<TAction, TObservation, MCTSMetadata, TState> root) {
        var node = root;
        while(!node.isLeaf()) {
            var bestAction = getBestAction(node);
            node = node.getChildNodeMap().get(bestAction);
//
//            int nodeVisitCount = node.getSearchNodeMetadata().getVisitCounter();
//            var entityInGameId = node.getStateWrapper().getInGameEntityOnTurnId();
//            var action = node.getChildNodeStream()
//                .collect(StreamUtils.toRandomizedMaxCollector(
//                    Comparator.comparing(
//                        o -> calculateUCBValue( // TODO: optimize
//                            o.getSearchNodeMetadata().getExpectedReward()[entityInGameId] + o.getSearchNodeMetadata().getGainedReward()[entityInGameId],
//                            explorationConstant,
//                            nodeVisitCount,
//                            o.getSearchNodeMetadata().getVisitCounter())),
//                    random))
//                .getAppliedAction();
//            node = node.getChildNodeMap().get(action);
        }
        return node;
    }

    protected double calculateUCBValue(double estimatedValue, double explorationConstant, int parentVisitCount, int actionVisitCount) {
        return estimatedValue + explorationConstant * Math.sqrt(Math.log(parentVisitCount) / (1.0 + actionVisitCount));

    }

}
