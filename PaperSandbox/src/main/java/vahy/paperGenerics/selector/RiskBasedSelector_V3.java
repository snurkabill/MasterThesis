package vahy.paperGenerics.selector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.SplittableRandom;

public class RiskBasedSelector_V3<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>>
    extends AbstractRiskAverseTreeBasedNodeSelector<TAction, TObservation, PaperMetadata<TAction>, TState> {

    private final double cpuctParameter;
    private final int totalPlayerActions;
    private final int[] indexArray;
    private final double[] valueArray;

    public RiskBasedSelector_V3(double cpuctParameter, SplittableRandom random, int playerTotalActionCount) {
        super(random);
        this.cpuctParameter = cpuctParameter;
        this.totalPlayerActions = playerTotalActionCount;
        this.indexArray = new int[totalPlayerActions];
        this.valueArray =  new double[totalPlayerActions];
    }

    protected TAction getBestAction(SearchNode<TAction, TObservation, PaperMetadata<TAction>, TState> node, double currentRisk) {
        TAction[] possibleActions = node.getAllPossibleActions();
        var searchNodeMap = node.getChildNodeMap();
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < possibleActions.length; i++) {
            var metadata = searchNodeMap.get(possibleActions[i]).getSearchNodeMetadata();
            double reward = metadata.getExpectedReward() + metadata.getGainedReward();
            if(max < reward) {
                max = reward;
            }
            if(min > reward) {
                min = reward;
            }
            valueArray[i] = reward;
        }

        int maxIndex = -1;
        double maxValue = -Double.MAX_VALUE;

        double currentRiskWeight = (1 - currentRisk);
        int totalNodeVisitCount = node.getSearchNodeMetadata().getVisitCounter();
        if(max != min) {
            var norm = max - min;
            for (int i = 0; i < possibleActions.length; i++) {
                var metadata = searchNodeMap.get(possibleActions[i]).getSearchNodeMetadata();
                var normalizedReward = (valueArray[i] - min) / norm;
                var risk = metadata.getExpectedRisk();
                var vValue = normalizedReward * (1 - risk * currentRiskWeight);
                var uValue = cpuctParameter * metadata.getPriorProbability() * Math.sqrt(totalNodeVisitCount / (1.0 + metadata.getVisitCounter()));
                var quValue = vValue + uValue;
                if(quValue > maxValue) {
                    maxIndex = i;
                    maxValue = quValue;
                }
            }
            return possibleActions[maxIndex];
        } else {
            int maxIndexCount = 0;
            for (int i = 0; i < possibleActions.length; i++) {
                var metadata = searchNodeMap.get(possibleActions[i]).getSearchNodeMetadata();
                var reward = metadata.getExpectedReward() + metadata.getGainedReward();
                var normalizedReward = (max == min ? 0.5 : ((reward - min) / (max - min)));
                var risk = metadata.getExpectedRisk();
                var vValue = normalizedReward * (1 - risk * currentRiskWeight);
                var uValue = cpuctParameter * metadata.getPriorProbability() * Math.sqrt(totalNodeVisitCount / (1.0 + metadata.getVisitCounter()));
                var quValue = vValue + uValue;
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
            return maxIndexCount == 0 ? possibleActions[maxIndex] : possibleActions[indexArray[random.nextInt(maxIndexCount)]];
        }
    }


    @Override
    public SearchNode<TAction, TObservation, PaperMetadata<TAction>, TState> selectNextNode() {
        checkRoot();
        var node = root;
        while(!node.isLeaf()) {
            var action = node.isPlayerTurn() ? getBestAction(node, allowedRiskInRoot) : sampleOpponentAction(node);
            node = node.getChildNodeMap().get(action);
        }
        return node;
    }

}
