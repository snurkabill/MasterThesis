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


public class OptimalFlowSoftConstraintDeprecated<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>
    extends AbstractLinearProgramOnTreeDeprecated<TAction, TObservation, TSearchNodeMetadata, TState> {

    private static final double RISK_COEFFICIENT = 1.0;

    private final CLPExpression totalRiskExpression;
    private final double totalRiskAllowed;

    public OptimalFlowSoftConstraintDeprecated(Class<TAction> actionClass, double totalRiskAllowed, SplittableRandom random, NoiseStrategy strategy) {
        super(actionClass, true, random, strategy);
        this.totalRiskExpression = model.createExpression();
        this.totalRiskAllowed = totalRiskAllowed;
    }

    @Override
    protected void setLeafObjective(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node) {
        var inGameEntityId = node.getStateWrapper().getInGameEntityId();
        if(((RiskStateWrapper<TAction, TObservation, TState>)node.getStateWrapper()).isRiskHit()) {
            totalRiskExpression.add(RISK_COEFFICIENT, node.getSearchNodeMetadata().getNodeProbabilityFlow());
        } else {
            totalRiskExpression.add(0.0, node.getSearchNodeMetadata().getNodeProbabilityFlow());
        }
        double cumulativeReward = node.getSearchNodeMetadata().getCumulativeReward()[inGameEntityId];
        double expectedReward = node.getSearchNodeMetadata().getExpectedReward()[inGameEntityId];
        double predictedRisk = node.getSearchNodeMetadata().getExpectedRisk()[inGameEntityId];
        double leafCoefficient = cumulativeReward + (expectedReward * (1 - predictedRisk));

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
