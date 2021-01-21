package vahy.ralph.policy.linearProgram.deprecated;

import com.quantego.clp.CLPExpression;
import vahy.RiskState;
import vahy.RiskStateWrapper;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.ralph.metadata.RalphMetadata;
import vahy.ralph.policy.linearProgram.NoiseStrategy;

import java.util.SplittableRandom;


public class OptimalFlowHardConstraintCalculatorDeprecated<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>
    extends AbstractLinearProgramOnTreeDeprecated<TAction, TObservation, TSearchNodeMetadata, TState> {

    private final CLPExpression totalRiskExpression;
    private final double totalRiskAllowed;

    public OptimalFlowHardConstraintCalculatorDeprecated(Class<TAction> actionClass, double totalRiskAllowed, SplittableRandom random, NoiseStrategy strategy) {
        super(actionClass, true, random, strategy);
        this.totalRiskExpression = model.createExpression();
        this.totalRiskAllowed = totalRiskAllowed;
    }


    @Override
    protected void setLeafObjective(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node) {
        var inGameEntityId = node.getStateWrapper().getInGameEntityId();
        double nodeRisk = ((RiskStateWrapper<TAction, TObservation, TState>)node.getStateWrapper()).isRiskHit() ? 1.0 : node.getSearchNodeMetadata().getExpectedRisk()[inGameEntityId];
        totalRiskExpression.add(nodeRisk, node.getSearchNodeMetadata().getNodeProbabilityFlow());
        double cumulativeReward = node.getSearchNodeMetadata().getCumulativeReward()[inGameEntityId];
        double expectedReward = node.getSearchNodeMetadata().getExpectedReward()[inGameEntityId];
        double leafCoefficient = cumulativeReward + expectedReward;

        if(strategy != NoiseStrategy.NONE) {
            var value = random.nextDouble(noiseLowerBound, noiseUpperBound);
            leafCoefficient = leafCoefficient + (random.nextBoolean() ? value : -value);
        }

        model.setObjectiveCoefficient(node.getSearchNodeMetadata().getNodeProbabilityFlow(), leafCoefficient);
    }

    @Override
    protected void finalizeHardConstraints() {
        this.totalRiskExpression.leq(totalRiskAllowed);
    }

}
