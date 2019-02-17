package vahy.paperGenerics.policy.linearProgram;

import com.quantego.clp.CLPExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;

import java.util.SplittableRandom;

public class OptimalFlowHardConstraintCalculator<
    TAction extends Action,
    TReward extends DoubleReward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractLinearProgramOnTree<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(OptimalFlowHardConstraintCalculator.class.getName());

    private final CLPExpression totalRiskExpression;

    public OptimalFlowHardConstraintCalculator(SplittableRandom random, double totalRiskAllowed) {
        super(random, true);
        this.totalRiskExpression = model.createExpression();
        this.totalRiskExpression.leq(totalRiskAllowed);
    }


    @Override
    protected void setLeafObjective(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node) {
        double nodeRisk = node.getWrappedState().isRiskHit() ? 1.0 : node.getSearchNodeMetadata().getPredictedRisk();
        totalRiskExpression.add(nodeRisk, node.getSearchNodeMetadata().getNodeProbabilityFlow());
        double cumulativeReward = node.getSearchNodeMetadata().getCumulativeReward().getValue();
        double expectedReward = node.getSearchNodeMetadata().getExpectedReward().getValue();
        double leafCoefficient = cumulativeReward + expectedReward;
        model.setObjectiveCoefficient(node.getSearchNodeMetadata().getNodeProbabilityFlow(), leafCoefficient);
    }

}
