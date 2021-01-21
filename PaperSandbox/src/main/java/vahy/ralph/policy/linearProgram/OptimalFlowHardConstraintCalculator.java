package vahy.ralph.policy.linearProgram;

import com.quantego.clp.CLPExpression;
import vahy.RiskState;
import vahy.RiskStateWrapper;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.ralph.metadata.RalphMetadata;

import java.util.SplittableRandom;

public class OptimalFlowHardConstraintCalculator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>
    extends AbstractLinearProgramOnTreeWithFixedOpponents<TAction, TObservation, TSearchNodeMetadata, TState> {

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
        double nodeRisk = ((RiskStateWrapper<TAction, TObservation, TState>)node.getStateWrapper()).isRiskHit() ? 1.0 : metadata.getExpectedRisk()[inGameEntityId];
        totalRiskExpression.add(nodeRisk * element.modifier, element.flowWithCoefficient.closestParentFlow);
        element.flowWithCoefficient.coefficient += getNodeValue(metadata, inGameEntityId) * element.modifier;
    }

    @Override
    protected void finalizeHardConstraints() {
        this.totalRiskExpression.leq(totalRiskAllowed);
    }



}
