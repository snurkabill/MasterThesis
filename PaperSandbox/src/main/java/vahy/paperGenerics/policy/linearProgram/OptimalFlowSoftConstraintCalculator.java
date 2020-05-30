package vahy.paperGenerics.policy.linearProgram;

import com.quantego.clp.CLPExpression;
import com.quantego.clp.CLPVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.List;
import java.util.SplittableRandom;

public class OptimalFlowSoftConstraintCalculator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    extends AbstractLinearProgramOnTree<TAction, TObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(OptimalFlowSoftConstraintCalculator.class.getName());

    private static final double RISK_COEFFICIENT = 1.0;

    private final CLPExpression totalRiskExpression;
    private final double totalRiskAllowed;

    public OptimalFlowSoftConstraintCalculator(double totalRiskAllowed, SplittableRandom random, NoiseStrategy strategy) {
        super(true, random, strategy);
        this.totalRiskExpression = model.createExpression();
        this.totalRiskAllowed = totalRiskAllowed;
    }

    @Override
    protected void setLeafObjective(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node) {
        var metadata = node.getSearchNodeMetadata();
        totalRiskExpression.add(node.getStateWrapper().isRiskHit() ? 1.0 : 0.0, metadata.getNodeProbabilityFlow());
        double cumulativeReward = metadata.getCumulativeReward();
        double expectedReward = metadata.getExpectedReward();
        double predictedRisk = metadata.getPredictedRisk();
        double leafCoefficient = cumulativeReward + (expectedReward * (1 - predictedRisk));
        model.setObjectiveCoefficient(metadata.getNodeProbabilityFlow(), addNoiseToLeaf(leafCoefficient));
    }

    @Override
    protected void setLeafObjectiveWithFlow(List<SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> nodeList, CLPVariable parentFlow) {
        double sum = 0.0;
        for (SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> entry : nodeList) {
            var metadata = entry.getSearchNodeMetadata();
            double priorProbability = metadata.getPriorProbability();
            totalRiskExpression.add((entry.getStateWrapper().isRiskHit() ? 1.0 : 0.0) * priorProbability, parentFlow);
            double cumulativeReward = metadata.getCumulativeReward();
            double expectedReward = metadata.getExpectedReward();
            double predictedRisk = metadata.getPredictedRisk();
            double leafCoefficient = cumulativeReward + (expectedReward * (1 - predictedRisk));
            sum += leafCoefficient * priorProbability;
        }
        model.setObjectiveCoefficient(parentFlow, sum);
    }

    @Override
    protected void finalizeHardConstraints() {
        this.totalRiskExpression.leq(totalRiskAllowed);
    }
}
