package vahy.paperGenerics.selector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.ArrayList;
import java.util.SplittableRandom;

public class RiskBasedSelector_V3<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractRiskAverseTreeBasedNodeSelector<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> {

    private final double cpuctParameter;
    private final int playerTotalActionCount;
    private final double[] values;

    public RiskBasedSelector_V3(double cpuctParameter, SplittableRandom random, int playerTotalActionCount) {
        super(random);
        this.cpuctParameter = cpuctParameter;
        this.playerTotalActionCount = playerTotalActionCount;
        this.values = new double[playerTotalActionCount];
    }

    protected TAction getBestAction(SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> node, double currentRisk) {
        int totalNodeVisitCount = node.getSearchNodeMetadata().getVisitCounter();
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        double currentRiskWeight = (1 - currentRisk);
        var childNodeMap = node.getChildNodeMap();
        for (var entry : childNodeMap.values()) {
            var metadata = entry.getSearchNodeMetadata();
            double reward = metadata.getExpectedReward() + metadata.getGainedReward();
            if(max < reward) {
                max = reward;
            }
            if(min > reward) {
                min = reward;
            }
        }

        TAction[] possibleActions = node.getAllPossibleActions();
        var searchNodeMap = node.getChildNodeMap();

        int maxIndex = -1;
        double maxValue = -Double.MAX_VALUE;
        var indexList = new ArrayList<Integer>();

        for (int i = 0; i < possibleActions.length; i++) {
            var metadata = searchNodeMap.get(possibleActions[i]).getSearchNodeMetadata();
            var reward = metadata.getExpectedReward() + metadata.getGainedReward();
            var normalizedReward = (max == min ? 0.5 : ((reward - min) / (max - min)));
            var risk = metadata.getPredictedRisk();
            var vValue = normalizedReward * (1 - risk * currentRiskWeight);
            var uValue = cpuctParameter * metadata.getPriorProbability() * Math.sqrt(totalNodeVisitCount / (1.0 + metadata.getVisitCounter()));
            var quValue = vValue + uValue;
            if(quValue > maxValue) {
                maxIndex = i;
                maxValue = quValue;
                indexList.clear();
            } else if(quValue == maxValue) {
                if(indexList.isEmpty()) {
                    indexList.add(maxIndex);
                    indexList.add(i);
                } else {
                    indexList.add(i);
                }
            }
        }
        return indexList.isEmpty() ? possibleActions[maxIndex] : possibleActions[indexList.get(random.nextInt(indexList.size()))];
    }

    protected double calculateUValue(double priorProbability, int childVisitCount, int nodeTotalVisitCount) {
        return cpuctParameter * priorProbability * Math.sqrt(nodeTotalVisitCount / (1.0 + childVisitCount));
    }

    @Override
    public SearchNode<TAction, TPlayerObservation, TOpponentObservation, PaperMetadata<TAction>, TState> selectNextNode() {
        checkRoot();
        var node = root;
        while(!node.isLeaf()) {
            var action = node.isPlayerTurn() ? getBestAction(node, allowedRiskInRoot) : sampleOpponentAction(node);
            node = node.getChildNodeMap().get(action);
        }
        return node;
    }

}
