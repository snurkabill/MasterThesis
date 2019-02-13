package vahy.paperGenerics.policy;

import com.quantego.clp.CLP;
import com.quantego.clp.CLPExpression;
import com.quantego.clp.CLPVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.collections.RandomIterator;
import vahy.impl.model.reward.DoubleReward;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.SplittableRandom;

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

    private final SplittableRandom random;

    public OptimalFlowCalculator(SplittableRandom random) {
        this.random = random;
    }

    public boolean calculateFlow(SearchNode<TAction, TReward, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> root, double totalRiskAllowed) {
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
                var entries = node.getChildNodeMap().entrySet();
                var nodeChildIterator = new RandomIterator<>(entries.iterator(), random);
//                var nodeChildIterator = entries.iterator();

                while(nodeChildIterator.hasNext()) {
                    var entry = nodeChildIterator.next();
                    queue.addLast(entry.getValue());
                    CLPVariable childFlow = model.addVariable().lb(LOWER_BOUND).ub(UPPER_BOUND);
                    entry.getValue().getSearchNodeMetadata().setNodeProbabilityFlow(childFlow);
                    actionChildFlowMap.put(entry.getKey(), childFlow);
                }
            }

            if(node.isLeaf()) {
                if(totalRiskExpression == null) {
                    totalRiskExpression = model.createExpression();
                }
                double nodeRisk = node.getWrappedState().isRiskHit() ? 1.0 : node.getSearchNodeMetadata().getPredictedRisk();
                totalRiskExpression.add(nodeRisk, node.getSearchNodeMetadata().getNodeProbabilityFlow());


                double cumulativeReward = node.getSearchNodeMetadata().getCumulativeReward().getValue();
                double expectedReward = node.getSearchNodeMetadata().getExpectedReward().getValue();

                double leafCoefficient = cumulativeReward + expectedReward;


                model.setObjectiveCoefficient(node.getSearchNodeMetadata().getNodeProbabilityFlow(), leafCoefficient);
            } else {
                addSummingChildrenWithParentToZeroExpression(model, node, actionChildFlowMap);
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
            logger.error("Optimal solution was not found.");
            return false;
//            throw new IllegalStateException("Optimal solution was not found");
        }
        long finishOptimalization = System.currentTimeMillis();
        logger.debug("Optimizing linear program took [{}] ms", finishOptimalization - startOptimalization);
        return true;
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

    public void addSummingChildrenWithParentToZeroExpression(CLP model,
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
