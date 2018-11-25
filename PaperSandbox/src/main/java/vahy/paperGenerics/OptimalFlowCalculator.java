package vahy.paperGenerics;

import com.quantego.clp.CLP;
import com.quantego.clp.CLPExpression;
import com.quantego.clp.CLPVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.search.node.SearchNode;
import vahy.environment.ActionType;
import vahy.environment.state.ImmutableStateImpl;
import vahy.impl.model.observation.DoubleVectorialObservation;
import vahy.impl.model.reward.DoubleScalarReward;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class OptimalFlowCalculator {

    private static final Logger logger = LoggerFactory.getLogger(OptimalFlowCalculator.class.getName());

    private static final double LOWER_BOUND = 0.0;
    private static final double UPPER_BOUND = 1.0;
    private static final double CHILD_VARIABLE_COEFFICIENT = 1.0;
    private static final double PARENT_VARIABLE_COEFFICIENT = -1.0;
    private static final double RISK_COEFFICIENT = 1.0;

    public double calculateFlow(SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, PaperMetadata<ActionType, DoubleScalarReward>, ImmutableStateImpl> root, double totalRiskAllowed) {
        long startBuildingLinearProgram = System.currentTimeMillis();
        CLP model = new CLP();
        LinkedList<SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, PaperMetadata<ActionType, DoubleScalarReward>, ImmutableStateImpl>> queue = new LinkedList<>();
        queue.addFirst(root);

        root.getSearchNodeMetadata().setNodeProbabilityFlow(model.addVariable().lb(UPPER_BOUND).ub(UPPER_BOUND));

        CLPExpression totalRiskExpression = null;
        while(!queue.isEmpty()) {
            SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, PaperMetadata<ActionType, DoubleScalarReward>, ImmutableStateImpl> node = queue.poll();
            Map<ActionType, CLPVariable> actionChildFlowMap = new HashMap<>();
            if(!node.isLeaf()) {
                for (Map.Entry<ActionType, SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, PaperMetadata<ActionType, DoubleScalarReward>, ImmutableStateImpl>> entry : node.getChildNodeMap().entrySet()) {
                    queue.addLast(entry.getValue());
                    CLPVariable childFlow = model.addVariable().lb(LOWER_BOUND).ub(UPPER_BOUND);
                    entry.getValue().getSearchNodeMetadata().setNodeProbabilityFlow(childFlow);
                    actionChildFlowMap.put(entry.getKey(), childFlow);
                }
            }

            if(node.isLeaf()) {
                if(node.getWrappedState().isAgentKilled()) {
                    if(totalRiskExpression == null) {
                        totalRiskExpression = model.createExpression();
                    }
                    totalRiskExpression.add(RISK_COEFFICIENT, node.getSearchNodeMetadata().getNodeProbabilityFlow());
                }
                model.setObjectiveCoefficient(
                    node.getSearchNodeMetadata().getNodeProbabilityFlow(),
                    (node.getSearchNodeMetadata().getCumulativeReward().getValue() +
                        (node.getSearchNodeMetadata().getPredictedReward() != null ? node.getSearchNodeMetadata().getPredictedReward().getValue() : 0.0)
                    )
                        * (1 - node.getSearchNodeMetadata().getPredictedRisk())
                );
            } else {
                addSummingChildrenToOneExpression(model, node, actionChildFlowMap);
                if(!node.getWrappedState().isAgentTurn()) {
                    addChildFlowBasedOnFixedProbabilitiesExpression(model, node, actionChildFlowMap);
                }
            }
        }
        if(totalRiskExpression != null) {
            totalRiskExpression.leq(totalRiskAllowed);
        }
        long finishBuildingLinearProgram = System.currentTimeMillis();
        logger.debug("Building linear program took [{}]ms", finishBuildingLinearProgram - startBuildingLinearProgram);
        long startOptimalization = System.currentTimeMillis();
        CLP.STATUS status = model.maximize();
        if(status != CLP.STATUS.OPTIMAL) {
            throw new IllegalStateException("Optimal solution was not found");
//            if(root.getNodeProbabilityFlow().getSolution() == 0.0) {
//                System.out.println("fak me");
//            }
//            return -100000;
        }
        long finishOptimalization = System.currentTimeMillis();
        logger.debug("Optimizing linear program took [{}] ms", finishOptimalization - startOptimalization);
        return model.getObjectiveValue();
    }

    public void addChildFlowBasedOnFixedProbabilitiesExpression(CLP model,
                                                                SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, PaperMetadata<ActionType, DoubleScalarReward>, ImmutableStateImpl> node,
                                                                Map<ActionType, CLPVariable> actionChildFlowMap) {
        for (Map.Entry<ActionType, CLPVariable> entry : actionChildFlowMap.entrySet()) {
            SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, PaperMetadata<ActionType, DoubleScalarReward>, ImmutableStateImpl> child = node.getChildNodeMap().get(entry.getKey());
            double priorProbability = child.getSearchNodeMetadata().getPriorProbability();
            CLPExpression fixedProbabilityExpression = model.createExpression();
            fixedProbabilityExpression.add(CHILD_VARIABLE_COEFFICIENT, child.getSearchNodeMetadata().getNodeProbabilityFlow());
            fixedProbabilityExpression.add(PARENT_VARIABLE_COEFFICIENT * priorProbability, node.getSearchNodeMetadata().getNodeProbabilityFlow());
            fixedProbabilityExpression.eq(0.0);
        }
    }

    public void addSummingChildrenToOneExpression(CLP model,
                                                  SearchNode<ActionType, DoubleScalarReward, DoubleVectorialObservation, PaperMetadata<ActionType, DoubleScalarReward>, ImmutableStateImpl> node,
                                                  Map<ActionType, CLPVariable> actionChildFlowMap) {
        CLPExpression parentFlowDistribution = model.createExpression();
        for (Map.Entry<ActionType, CLPVariable> childFlowVariable : actionChildFlowMap.entrySet()) {
            parentFlowDistribution.add(CHILD_VARIABLE_COEFFICIENT, childFlowVariable.getValue());
        }
        parentFlowDistribution.add(PARENT_VARIABLE_COEFFICIENT, node.getSearchNodeMetadata().getNodeProbabilityFlow());
        parentFlowDistribution.eq(0.0);
    }

}
