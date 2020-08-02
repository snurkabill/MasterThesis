package vahy.paperGenerics.selector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.SplittableRandom;

public class RiskBasedSelector_V3<TAction extends Enum<TAction> & Action, TObservation extends Observation, TState extends State<TAction, TObservation, TState>> extends PaperNodeSelector<TAction, TObservation, TState> {

    public RiskBasedSelector_V3(SplittableRandom random, boolean isModelKnown, double cpuctParameter, int totalActionCount) {
        super(random, isModelKnown, cpuctParameter, totalActionCount);

    }

    @Override
    protected TAction getBestAction_inner(SearchNode<TAction, TObservation, PaperMetadata<TAction>, TState> node) {
        var wrapper = node.getStateWrapper();
        if(wrapper.getInGameEntityId() == wrapper.getInGameEntityOnTurnId()) {
            return getBestActionWithRisk(node, allowedRiskInRoot);
        } else {
            return super.getBestAction_inner(node);
        }
    }

    protected TAction getBestActionWithRisk(SearchNode<TAction, TObservation, PaperMetadata<TAction>, TState> node, double currentRisk) {
        var entityInGameOnTurn = node.getStateWrapper().getInGameEntityOnTurnId();
        TAction[] possibleActions = node.getAllPossibleActions();
        var searchNodeMap = node.getChildNodeMap();
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < possibleActions.length; i++) {
            var metadata = searchNodeMap.get(possibleActions[i]).getSearchNodeMetadata();
            double reward = metadata.getExpectedReward()[entityInGameOnTurn] + metadata.getGainedReward()[entityInGameOnTurn];
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
        if(max > min) {
            var norm = max - min;
            for (int i = 0; i < possibleActions.length; i++) {
                var metadata = searchNodeMap.get(possibleActions[i]).getSearchNodeMetadata();
                var normalizedReward = (valueArray[i] - min) / norm;
                var risk = metadata.getExpectedRisk()[entityInGameOnTurn];
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
                var reward = metadata.getExpectedReward()[entityInGameOnTurn] + metadata.getGainedReward()[entityInGameOnTurn];
                var normalizedReward = (max == min ? 0.5 : ((reward - min) / (max - min)));
                var risk = metadata.getExpectedRisk()[entityInGameOnTurn];
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

}
