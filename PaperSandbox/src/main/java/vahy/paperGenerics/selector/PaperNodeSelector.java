package vahy.paperGenerics.selector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.SplittableRandom;

public class PaperNodeSelector<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractRiskAverseTreeBasedNodeSelector<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> {

    private static Logger logger = LoggerFactory.getLogger(PaperNodeSelector.class.getName());
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled();

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

    protected TAction getBestAction(SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> node) {
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
            TAction action = possibleActions[maxIndex];
            if(TRACE_ENABLED) {
                logger.trace("Selecting unique action: [{}]", action);
            }
            return action;
        } else {
            var randomIndex = random.nextInt(maxIndexCount);
            var action = possibleActions[indexArray[randomIndex]];
            if(TRACE_ENABLED) {
                logger.trace("Selecting action: [{}] with random index [{}] from action count: [{}]", action, randomIndex, maxIndexCount);
            }
            return action;
        }
    }

    protected double calculateUValue(double priorProbability, int childVisitCount, int nodeTotalVisitCount) {
        return cpuctParameter * priorProbability * Math.sqrt(nodeTotalVisitCount / (1.0 + childVisitCount));
    }

    @Override
    public SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> selectNextNode() {
        checkRoot();
        var node = root;
        while(!node.isLeaf()) {
            logger.trace("Selected node: [{}]", node.toString());
            var action = node.isPlayerTurn() ? getBestAction(node) : sampleOpponentAction(node);
            logger.trace("Selected next action: [{}]", action.toString());
            node = node.getChildNodeMap().get(action);
        }
        return node;
    }
}
