package vahy.paperGenerics.selector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.SplittableRandom;

public class PaperNodeSelector<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TState extends State<TAction, TObservation, TState>>
    extends AbstractRiskAverseTreeBasedNodeSelector<TAction, TObservation, PaperMetadata<TAction>, TState> {


    private final double cpuctParameter;
    private final int totalPlayerActions;
    private final int[] indexArray;
    private final double[] valueArray;

    public PaperNodeSelector(double cpuctParameter, SplittableRandom random, int totalPlayerActions) {
        super(random);
        this.cpuctParameter = cpuctParameter;
        this.totalPlayerActions = totalPlayerActions;
        this.indexArray = new int[totalPlayerActions];
        this.valueArray =  new double[totalPlayerActions];
    }

    protected TAction getBestAction(SearchNode<TAction, TObservation, PaperMetadata<TAction>, TState> node) {
        TAction[] possibleActions = node.getAllPossibleActions();
        var searchNodeMap = node.getChildNodeMap();

        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < possibleActions.length; i++) {
            var metadata = searchNodeMap.get(possibleActions[i]).getSearchNodeMetadata();
            double value = metadata.getExpectedReward() + metadata.getGainedReward();
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
                var uValue = cpuctParameter * metadata.getPriorProbability() * Math.sqrt(totalNodeVisitCount / (1.0 + metadata.getVisitCounter()));
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
                var quValue = cpuctParameter * metadata.getPriorProbability() * Math.sqrt(totalNodeVisitCount / (1.0 + metadata.getVisitCounter()));
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

    protected double calculateUValue(double priorProbability, int childVisitCount, int nodeTotalVisitCount) {
        return cpuctParameter * priorProbability * Math.sqrt(nodeTotalVisitCount / (1.0 + childVisitCount));
    }

    @Override
    public SearchNode<TAction, TObservation, PaperMetadata<TAction>, TState> selectNextNode() {
        checkRoot();
        var node = root;
        while(!node.isLeaf()) {
            var action = node.isPlayerTurn() ? getBestAction(node) : sampleOpponentAction(node);
            node = node.getChildNodeMap().get(action);
        }
        return node;
    }
}
