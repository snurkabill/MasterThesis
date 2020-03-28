package vahy.paperGenerics.policy.linearProgram;

import com.quantego.clp.CLPExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.PaperState;

import java.util.SplittableRandom;

public class OptimalFlowSoftConstraint<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractLinearProgramOnTree<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(OptimalFlowSoftConstraint.class.getName());

    private static final double RISK_COEFFICIENT = 1.0;

    private final CLPExpression totalRiskExpression;
    private final double totalRiskAllowed;

    public OptimalFlowSoftConstraint(Class<TAction> actionClass, double totalRiskAllowed, SplittableRandom random, NoiseStrategy strategy) {
        super(actionClass, true, random, strategy);
        this.totalRiskExpression = model.createExpression();
        this.totalRiskAllowed = totalRiskAllowed;
    }

    @Override
    protected void setLeafObjective(SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        if(node.getWrappedState().isRiskHit()) {
            totalRiskExpression.add(RISK_COEFFICIENT, node.getSearchNodeMetadata().getNodeProbabilityFlow());
        } else {
            totalRiskExpression.add(0.0, node.getSearchNodeMetadata().getNodeProbabilityFlow());
        }
        double cumulativeReward = node.getSearchNodeMetadata().getCumulativeReward();
        double expectedReward = node.getSearchNodeMetadata().getExpectedReward();
        double predictedRisk = node.getSearchNodeMetadata().getPredictedRisk();
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
