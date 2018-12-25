package vahy.paperGenerics.policy;

import com.quantego.clp.CLP;
import com.quantego.clp.CLPExpression;
import com.quantego.clp.CLPVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class OptimalFlowCalculator<
    TAction extends Action,
    TReward extends DoubleReward,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction, TReward>,
    TState extends PaperState<TAction, TReward, TPlayerObservation, TOpponentObservation, TState>> {

    private static final Logger logger = LoggerFactory.getLogger(OptimalFlowCalculator.class.getName());

    private static final double LOWER_BOUND = 0.0;
    private static final double UPPER_BOUND = 1.0;
    private static final double CHILD_VARIABLE_COEFFICIENT = 1.0;
    private static final double PARENT_VARIABLE_COEFFICIENT = -1.0;
    private static final double RISK_COEFFICIENT = 1.0;

    public double calculateFlow(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> root, double totalRiskAllowed) {
        long startBuildingLinearProgram = System.currentTimeMillis();
        CLP model = new CLP();
        LinkedList<SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> queue = new LinkedList<>();
        queue.addFirst(root);

        root.getSearchNodeMetadata().setNodeProbabilityFlow(model.addVariable().lb(UPPER_BOUND).ub(UPPER_BOUND));

        CLPExpression totalRiskExpression = null;
        while(!queue.isEmpty()) {
            SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node = queue.poll();
            Map<TAction, CLPVariable> actionChildFlowMap = new HashMap<>();
            if(!node.isLeaf()) {
                for (Map.Entry<TAction, SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> entry : node.getChildNodeMap().entrySet()) {
                    queue.addLast(entry.getValue());
                    CLPVariable childFlow = model.addVariable().lb(LOWER_BOUND).ub(UPPER_BOUND);
                    entry.getValue().getSearchNodeMetadata().setNodeProbabilityFlow(childFlow);
                    actionChildFlowMap.put(entry.getKey(), childFlow);
                }
            }

            if(node.isLeaf()) {
                if(node.getWrappedState().isRiskHit()) {
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
                if(!node.getWrappedState().isPlayerTurn()) {
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
                                                                SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node,
                                                                Map<TAction, CLPVariable> actionChildFlowMap) {
        for (Map.Entry<TAction, CLPVariable> entry : actionChildFlowMap.entrySet()) {
            SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> child = node.getChildNodeMap().get(entry.getKey());
            double priorProbability = child.getSearchNodeMetadata().getPriorProbability();
            CLPExpression fixedProbabilityExpression = model.createExpression();
            fixedProbabilityExpression.add(CHILD_VARIABLE_COEFFICIENT, child.getSearchNodeMetadata().getNodeProbabilityFlow());
            fixedProbabilityExpression.add(PARENT_VARIABLE_COEFFICIENT * priorProbability, node.getSearchNodeMetadata().getNodeProbabilityFlow());
            fixedProbabilityExpression.eq(0.0);
        }
    }

    public void addSummingChildrenToOneExpression(CLP model,
                                                  SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node,
                                                  Map<TAction, CLPVariable> actionChildFlowMap) {
        CLPExpression parentFlowDistribution = model.createExpression();
        for (Map.Entry<TAction, CLPVariable> childFlowVariable : actionChildFlowMap.entrySet()) {
            parentFlowDistribution.add(CHILD_VARIABLE_COEFFICIENT, childFlowVariable.getValue());
        }
        parentFlowDistribution.add(PARENT_VARIABLE_COEFFICIENT, node.getSearchNodeMetadata().getNodeProbabilityFlow());
        parentFlowDistribution.eq(0.0);
    }

}
