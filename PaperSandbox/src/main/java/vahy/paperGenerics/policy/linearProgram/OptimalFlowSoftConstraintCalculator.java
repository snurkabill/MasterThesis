package vahy.paperGenerics.policy.linearProgram;

import com.quantego.clp.CLPExpression;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.PaperStateWrapper;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.SplittableRandom;

public class OptimalFlowSoftConstraintCalculator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    extends AbstractLinearProgramOnTreeWithFixedOpponents<TAction, TObservation, TSearchNodeMetadata, TState> {

    private final CLPExpression totalRiskExpression;
    private final double totalRiskAllowed;

    public OptimalFlowSoftConstraintCalculator(double totalRiskAllowed, SplittableRandom random, NoiseStrategy strategy) {
        super(true, random, strategy);
        this.totalRiskExpression = model.createExpression();
        this.totalRiskAllowed = totalRiskAllowed;
    }

    @Override
    protected void setLeafObjective(InnerElement element) {
//        throw new IllegalStateException("BROKEN AND KNOWN. Fix me.");
        var node = element.node;
        var inGameEntityId = node.getStateWrapper().getInGameEntityId();
        var metadata = node.getSearchNodeMetadata();

        double nodeRisk = ((PaperStateWrapper<TAction, TObservation, TState>)node.getStateWrapper()).isRiskHit() ? 1.0 : metadata.getExpectedRisk()[inGameEntityId];
        totalRiskExpression.add(nodeRisk * element.modifier, element.flowWithCoefficient.closestParentFlow);


        double cumulativeReward = node.getSearchNodeMetadata().getCumulativeReward()[inGameEntityId];
        double expectedReward = node.getSearchNodeMetadata().getExpectedReward()[inGameEntityId];
        double predictedRisk = node.getSearchNodeMetadata().getExpectedRisk()[inGameEntityId];
        var nodeValue = addNoiseToLeaf(cumulativeReward + (expectedReward * (1.0 - predictedRisk)));

        element.flowWithCoefficient.coefficient += nodeValue * element.modifier;


//        var nodeRisk = ((PaperStateWrapper<TAction, TObservation, TState>)node.getStateWrapper()).isRiskHit() ? 1.0 : metadata.getExpectedRisk()[inGameEntityId];
//        totalRiskExpression.add(nodeRisk * element.modifier, element.flowWithCoefficient.closestParentFlow);
//        double cumulativeReward = metadata.getCumulativeReward()[inGameEntityId];
//        double expectedReward = metadata.getExpectedReward()[inGameEntityId];
//        double predictedRisk = metadata.getExpectedRisk()[inGameEntityId];
//        double leafCoefficient = cumulativeReward + (expectedReward * (1 - predictedRisk));
////        model.setObjectiveCoefficient(metadata.getNodeProbabilityFlow(), addNoiseToLeaf(leafCoefficient));
//        element.flowWithCoefficient.coefficient += getNodeValue(metadata, inGameEntityId) * element.modifier;
    }

//    @Override
//    protected void setLeafObjectiveWithFlow(List<SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> nodeList, CLPVariable parentFlow) {
//        double sum = 0.0;
//        for (SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> entry : nodeList) {
//            var inGameEntityId = entry.getStateWrapper().getInGameEntityId();
//            var metadata = entry.getSearchNodeMetadata();
//            double priorProbability = metadata.getPriorProbability();
//            totalRiskExpression.add((((PaperStateWrapper<TAction, TObservation, TState>)entry.getStateWrapper()).isRiskHit() ? 1.0 : 0.0) * priorProbability, parentFlow);
//            double cumulativeReward = metadata.getCumulativeReward()[inGameEntityId];
//            double expectedReward = metadata.getExpectedReward()[inGameEntityId];
//            double predictedRisk = metadata.getExpectedRisk()[inGameEntityId];
//            double leafCoefficient = cumulativeReward + (expectedReward * (1 - predictedRisk));
//            sum += leafCoefficient * priorProbability;
//        }
//        model.setObjectiveCoefficient(parentFlow, sum);
//    }

    @Override
    protected void finalizeHardConstraints() {
        this.totalRiskExpression.leq(totalRiskAllowed);
    }
}
