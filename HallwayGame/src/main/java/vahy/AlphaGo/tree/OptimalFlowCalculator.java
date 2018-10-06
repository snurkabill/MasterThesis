package vahy.AlphaGo.tree;

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

    private double totalRiskAllowed;

    public OptimalFlowCalculator(double totalRiskAllowed) {
        this.totalRiskAllowed = totalRiskAllowed;
    }

    public double calculateFlow(AlphaGoSearchNode root) {
        CLP model = new CLP();
        LinkedList<AlphaGoSearchNode> queue = new LinkedList<>();
        queue.addFirst(root);

        root.setNodeProbabilityFlow(model.addVariable().lb(UPPER_BOUND).ub(UPPER_BOUND));

        CLPExpression totalRiskExpression = null;
        while(!queue.isEmpty()) {
            AlphaGoSearchNode node = queue.poll();
            Map<ActionType, CLPVariable> actionChildFlowMap = new HashMap<>();
            for (Map.Entry<ActionType, AlphaGoSearchNode> entry : node.getChildMap().entrySet()) {
                queue.addLast(entry.getValue());
                CLPVariable childFlow = model.addVariable().lb(LOWER_BOUND).ub(UPPER_BOUND);
                entry.getValue().setNodeProbabilityFlow(childFlow);
                actionChildFlowMap.put(entry.getKey(), childFlow);
            }
            if(node.isLeaf()) {
                if(node.getWrappedState().isAgentKilled()) {
                    if(totalRiskExpression == null) {
                        totalRiskExpression = model.createExpression();
                    }
                    totalRiskExpression.add(RISK_COEFFICIENT, node.getNodeProbabilityFlow());
                }
                model.setObjectiveCoefficient(node.getNodeProbabilityFlow(), node.getCumulativeReward().getValue());
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
        CLP.STATUS status = model.maximize();
        if(status != CLP.STATUS.OPTIMAL) {
            //throw new IllegalStateException("Optimal solution was not found");
            return -100000;
        }
        return model.getObjectiveValue();
    }

    public void addChildFlowBasedOnFixedProbabilitiesExpression(CLP model, AlphaGoSearchNode node, Map<ActionType, CLPVariable> actionChildFlowMap) {
        for (Map.Entry<ActionType, CLPVariable> entry : actionChildFlowMap.entrySet()) {
            AlphaGoSearchNode child = node.getChildMap().get(entry.getKey());
            double priorProbability = node.getEdgeMetadataMap().get(entry.getKey()).getPriorProbability();
            CLPExpression fixedProbabilityExpression = model.createExpression();
            fixedProbabilityExpression.add(CHILD_VARIABLE_COEFFICIENT, child.getNodeProbabilityFlow());
            fixedProbabilityExpression.add(PARENT_VARIABLE_COEFFICIENT * priorProbability, node.getNodeProbabilityFlow());
            fixedProbabilityExpression.eq(0.0);
        }
    }

    public void addSummingChildrenToOneExpression(CLP model, AlphaGoSearchNode node, Map<ActionType, CLPVariable> actionChildFlowMap) {
        CLPExpression parentFlowDistribution = model.createExpression();
        for (Map.Entry<ActionType, CLPVariable> childFlowVariable : actionChildFlowMap.entrySet()) {
            parentFlowDistribution.add(CHILD_VARIABLE_COEFFICIENT, childFlowVariable.getValue());
        }
        parentFlowDistribution.add(PARENT_VARIABLE_COEFFICIENT, node.getNodeProbabilityFlow());
        parentFlowDistribution.eq(0.0);
    }

}
