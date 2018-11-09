package vahy.paper.tree;

import com.quantego.clp.CLP;
import com.quantego.clp.CLPExpression;
import com.quantego.clp.CLPVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.environment.ActionType;

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

    public double calculateFlow(SearchNode root, double totalRiskAllowed) {
        long startBuildingLinearProgram = System.currentTimeMillis();
        CLP model = new CLP();
        LinkedList<SearchNode> queue = new LinkedList<>();
        queue.addFirst(root);

        root.setNodeProbabilityFlow(model.addVariable().lb(UPPER_BOUND).ub(UPPER_BOUND));

        CLPExpression totalRiskExpression = null;
        while(!queue.isEmpty()) {
            SearchNode node = queue.poll();
            Map<ActionType, CLPVariable> actionChildFlowMap = new HashMap<>();
            if(!node.isLeaf()) {
                for (Map.Entry<ActionType, SearchNode> entry : node.getChildMap().entrySet()) {
                    queue.addLast(entry.getValue());
                    CLPVariable childFlow = model.addVariable().lb(LOWER_BOUND).ub(UPPER_BOUND);
                    entry.getValue().setNodeProbabilityFlow(childFlow);
                    actionChildFlowMap.put(entry.getKey(), childFlow);
                }
            }
            if(node.isLeaf()) {

                if(node.isFakeRisk()) { // this is only for That weird MC
                    if(totalRiskExpression == null) {
                        totalRiskExpression = model.createExpression();
                    }
                    totalRiskExpression.add(RISK_COEFFICIENT, node.getNodeProbabilityFlow());

                    model.setObjectiveCoefficient(
                        node.getNodeProbabilityFlow(),
                        (node.getCumulativeReward().getValue() +
                            (node.getEstimatedReward() != null ? node.getEstimatedReward().getValue() : 0.0)
                        )
                            * (1 - 1)
                    );

                } else {
                    if(node.getWrappedState().isAgentKilled()) {
                        if(totalRiskExpression == null) {
                            totalRiskExpression = model.createExpression();
                        }
                        totalRiskExpression.add(RISK_COEFFICIENT, node.getNodeProbabilityFlow());
                    }
                    model.setObjectiveCoefficient(
                        node.getNodeProbabilityFlow(),
                        (node.getCumulativeReward().getValue() +
                            (node.getEstimatedReward() != null ? node.getEstimatedReward().getValue() : 0.0)
                        )
                            * (1 - (node.getRealRisk() + node.getEstimatedRisk()))
                    );
                }
            } else {
                addSummingChildrenToOneExpression(model, node, actionChildFlowMap);
                if(!node.isAgentTurn()) {
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

    public void addChildFlowBasedOnFixedProbabilitiesExpression(CLP model, SearchNode node, Map<ActionType, CLPVariable> actionChildFlowMap) {
        for (Map.Entry<ActionType, CLPVariable> entry : actionChildFlowMap.entrySet()) {
            SearchNode child = node.getChildMap().get(entry.getKey());
            double priorProbability = node.getEdgeMetadataMap().get(entry.getKey()).getPriorProbability();
            CLPExpression fixedProbabilityExpression = model.createExpression();
            fixedProbabilityExpression.add(CHILD_VARIABLE_COEFFICIENT, child.getNodeProbabilityFlow());
            fixedProbabilityExpression.add(PARENT_VARIABLE_COEFFICIENT * priorProbability, node.getNodeProbabilityFlow());
            fixedProbabilityExpression.eq(0.0);
        }
    }

    public void addSummingChildrenToOneExpression(CLP model, SearchNode node, Map<ActionType, CLPVariable> actionChildFlowMap) {
        CLPExpression parentFlowDistribution = model.createExpression();
        for (Map.Entry<ActionType, CLPVariable> childFlowVariable : actionChildFlowMap.entrySet()) {
            parentFlowDistribution.add(CHILD_VARIABLE_COEFFICIENT, childFlowVariable.getValue());
        }
        parentFlowDistribution.add(PARENT_VARIABLE_COEFFICIENT, node.getNodeProbabilityFlow());
        parentFlowDistribution.eq(0.0);
    }

}
