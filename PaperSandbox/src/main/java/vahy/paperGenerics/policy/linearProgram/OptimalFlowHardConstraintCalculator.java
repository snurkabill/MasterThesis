package vahy.paperGenerics.policy.linearProgram;

import com.quantego.clp.CLPExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.PaperStateWrapper;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.SplittableRandom;

public class OptimalFlowHardConstraintCalculator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    extends AbstractLinearProgramOnTreeWithFixedOpponents<TAction, TObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(OptimalFlowHardConstraintCalculator.class.getName());

    private final CLPExpression totalRiskExpression;
    private final double totalRiskAllowed;

    public OptimalFlowHardConstraintCalculator(double totalRiskAllowed, SplittableRandom random, NoiseStrategy strategy) {
        super(true, random, strategy);
        this.totalRiskExpression = model.createExpression();
        this.totalRiskAllowed = totalRiskAllowed;
    }

    @Override
    protected void setLeafObjective(InnerElement element) {
        var node = element.node;
        var inGameEntityId = node.getStateWrapper().getInGameEntityId();
        var metadata = node.getSearchNodeMetadata();
        double nodeRisk = ((PaperStateWrapper<TAction, TObservation, TState>)node.getStateWrapper()).isRiskHit() ? 1.0 : metadata.getExpectedRisk()[inGameEntityId];
        totalRiskExpression.add(nodeRisk * element.modifier, element.flowWithCoefficient.closestParentFlow);
        element.flowWithCoefficient.coefficient += getNodeValue(metadata, inGameEntityId) * element.modifier;
    }

//    @Override
//    protected void setLeafObjectiveWithFlow(List<InnerElement> nodeList, CLPVariable closestParentFlow) {
//        double sum = 0.0;
//        for (InnerElement element : nodeList) {
//            var entry = element.node;
//            var priorProbabilityFlow = element.modifier;
//            var inGameEntityId = entry.getStateWrapper().getInGameEntityId();
//            var metadata = entry.getSearchNodeMetadata();
//            double nodeRisk = ((PaperStateWrapper<TAction, TObservation, TState>)entry.getStateWrapper()).isRiskHit() ? 1.0 : metadata.getExpectedRisk()[inGameEntityId];
//            totalRiskExpression.add(nodeRisk * priorProbabilityFlow, closestParentFlow);
//            sum += getNodeValue(metadata, inGameEntityId) * priorProbabilityFlow;
//        }
//        model.setObjectiveCoefficient(closestParentFlow, sum);
//    }

    @Override
    protected void finalizeHardConstraints() {
        this.totalRiskExpression.leq(totalRiskAllowed);
    }



}
