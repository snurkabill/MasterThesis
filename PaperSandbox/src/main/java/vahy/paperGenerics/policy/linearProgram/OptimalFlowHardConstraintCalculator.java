package vahy.paperGenerics.policy.linearProgram;

import com.quantego.clp.CLPExpression;
import com.quantego.clp.CLPVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.paperGenerics.PaperStateWrapper;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;

import java.util.List;
import java.util.SplittableRandom;

public class OptimalFlowHardConstraintCalculator<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    extends AbstractLinearProgramOnTree<TAction, TObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(OptimalFlowHardConstraintCalculator.class.getName());

    private final CLPExpression totalRiskExpression;
    private final double totalRiskAllowed;

    public OptimalFlowHardConstraintCalculator(double totalRiskAllowed, SplittableRandom random, NoiseStrategy strategy) {
        super(true, random, strategy);
        this.totalRiskExpression = model.createExpression();
        this.totalRiskAllowed = totalRiskAllowed;
    }

    @Override
    protected void setLeafObjective(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node) {
        var metadata = node.getSearchNodeMetadata();
        double nodeRisk = ((PaperStateWrapper<TAction, TObservation, TState>)node.getStateWrapper()).isRiskHit() ? 1.0 : metadata.getPredictedRisk();
        totalRiskExpression.add(nodeRisk, metadata.getNodeProbabilityFlow());
        model.setObjectiveCoefficient(metadata.getNodeProbabilityFlow(), getNodeValue(metadata));
    }

    @Override
    protected void setLeafObjectiveWithFlow(List<SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> nodeList, CLPVariable parentFlow) {
        double sum = 0.0;
        for (SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> entry : nodeList) {
            var metadata = entry.getSearchNodeMetadata();
            double nodeRisk = ((PaperStateWrapper<TAction, TObservation, TState>)entry.getStateWrapper()).isRiskHit() ? 1.0 : metadata.getPredictedRisk();
            double priorProbability = metadata.getPriorProbability();
            totalRiskExpression.add(nodeRisk * priorProbability, parentFlow);
            sum += getNodeValue(metadata) * priorProbability;
        }
        model.setObjectiveCoefficient(parentFlow, sum);
    }

    @Override
    protected void finalizeHardConstraints() {
        this.totalRiskExpression.leq(totalRiskAllowed);
    }



}
