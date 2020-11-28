package vahy.impl.policy.mcts;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.api.search.nodeSelector.RandomizedNodeSelector;

import java.util.SplittableRandom;

public class UnfairUcb1NodeSelector<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TState extends State<TAction, TObservation, TState>>
    extends RandomizedNodeSelector<TAction, TObservation, MCTSMetadata, TState> {

    protected final double cpuctParameter;
    private final double[] valueArray;

    public UnfairUcb1NodeSelector(SplittableRandom random, double cpuctParameter, int maxBranchingCount) {
        super(random);
        this.cpuctParameter = cpuctParameter;
        this.valueArray = new double[maxBranchingCount];
    }

    protected TAction getBestAction(SearchNode<TAction, TObservation, MCTSMetadata, TState> node) {
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
        if(max > min) {
            var norm = max - min;
            for (int i = 0; i < possibleActions.length; i++) {
                var metadata = searchNodeMap.get(possibleActions[i]).getSearchNodeMetadata();
                var vValue = (valueArray[i] - min) / norm;
                var uValue = cpuctParameter * Math.sqrt(totalNodeVisitCount / (1.0 + metadata.getVisitCounter()));
                var quValue = vValue + uValue;
                if(quValue > maxValue) {
                    maxIndex = i;
                    maxValue = quValue;
                }
            }
        } else {
            for (int i = 0; i < possibleActions.length; i++) {
                var metadata = searchNodeMap.get(possibleActions[i]).getSearchNodeMetadata();
                var quValue = cpuctParameter * Math.sqrt(totalNodeVisitCount / (1.0 + metadata.getVisitCounter()));
                if(quValue > maxValue) {
                    maxIndex = i;
                    maxValue = quValue;
                }
            }
        }
        return possibleActions[maxIndex];
    }

    @Override
    public SearchNode<TAction, TObservation, MCTSMetadata, TState> selectNextNode(SearchNode<TAction, TObservation, MCTSMetadata, TState> root) {
        var node = root;
        while(!node.isLeaf()) {
            var bestAction = getBestAction(node);
            node = node.getChildNodeMap().get(bestAction);
        }
        return node;
    }

    protected double calculateUCBValue(double estimatedValue, double explorationConstant, int parentVisitCount, int actionVisitCount) {
        return estimatedValue + explorationConstant * Math.sqrt(Math.log(parentVisitCount) / (1.0 + actionVisitCount));

    }

}
