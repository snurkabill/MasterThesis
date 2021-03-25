package vahy.ralph.policy.linearProgram;

import com.quantego.clp.CLP;
import com.quantego.clp.CLPExpression;
import com.quantego.clp.CLPVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.RiskState;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.ralph.metadata.RalphMetadata;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.SplittableRandom;

public abstract class AbstractLinearProgramOnTreeWithFixedOpponents<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>  {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLinearProgramOnTreeWithFixedOpponents.class.getName());
    public static final boolean DEBUG_ENABLED = logger.isDebugEnabled();
    public static final boolean TRACE_ENABLED = logger.isTraceEnabled() || DEBUG_ENABLED;

    private static final double FLOW_TOLERANCE = 1.0 - Math.pow(10, -6);

    private static final double LOWER_BOUND = 0.0;
    private static final double UPPER_BOUND = 1.0;
    private static final double CHILD_VARIABLE_COEFFICIENT = 1.0;

    private final boolean maximize;
    private final NoiseStrategy strategy;
    private final SplittableRandom random;
    private final double noiseUpperBound;
    private final double noiseLowerBound;
    protected CLP model;

    private final Deque<InnerElement<TAction, TObservation, TSearchNodeMetadata, TState>> masterQueue;
    private final Deque<FlowWithCoefficient> flowList;

    protected AbstractLinearProgramOnTreeWithFixedOpponents(boolean maximize, SplittableRandom random, NoiseStrategy strategy) {
        this.model = new CLP();
//        this.model.algorithm(CLP.ALGORITHM.PRIMAL);
        this.masterQueue = new ArrayDeque<>();
        this.flowList = new ArrayDeque<>();
        this.maximize = maximize;
        this.random = random;
        this.strategy = strategy;
        this.noiseLowerBound = strategy.getLowerBound();
        this.noiseUpperBound = strategy.getUpperBound();
    }

    protected abstract void setLeafObjective(InnerElement<TAction, TObservation, TSearchNodeMetadata, TState> node);
//    protected abstract void setLeafObjectiveWithFlow(List<InnerElement> nodeList, CLPVariable parentFlow);

    protected abstract void finalizeHardConstraints();

    public double getObjectiveValue() {
        return model.getObjectiveValue();
    }

    public boolean optimizeFlow(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> root) {
        long startBuildingLinearProgram = System.currentTimeMillis();
        initializeQueues(root);
        while(!masterQueue.isEmpty()) {
            var innerElement = masterQueue.pop();
            if(!innerElement.node.isLeaf()) {
                if(innerElement.node.isPlayerTurn()) {
                    CLPExpression parentFlowDistribution = model.createExpression();
                    for (var node : innerElement.node.getChildNodeMap().values()) {
                        CLPVariable childFlow = model.addVariable().lb(LOWER_BOUND).ub(UPPER_BOUND);
                        node.getSearchNodeMetadata().setNodeProbabilityFlow(childFlow);
                        parentFlowDistribution.add(CHILD_VARIABLE_COEFFICIENT, childFlow);
                        var flowWithCoefficient = new FlowWithCoefficient(childFlow);
                        masterQueue.add(new InnerElement<>(node, 1.0, flowWithCoefficient));
                        flowList.add(flowWithCoefficient);
                    }
                    parentFlowDistribution.add(-innerElement.modifier, innerElement.flowWithCoefficient.getClosestParentFlow());
                    parentFlowDistribution.eq(0.0);
                } else {
                    for (var node : innerElement.node.getChildNodeMap().values()) {
                        masterQueue.add(new InnerElement<>(node, innerElement.modifier * node.getSearchNodeMetadata().getPriorProbability(), innerElement.flowWithCoefficient));
                    }
                }
            } else {
                setLeafObjective(innerElement);
            }
        }

        finalizeFlowCoefficients();
        finalizeHardConstraints();
        if(TRACE_ENABLED) {
            logger.trace("Building linear program took [{}]ms", System.currentTimeMillis() - startBuildingLinearProgram);
        }
        long startOptimization = System.currentTimeMillis();
        CLP.STATUS status = maximize ? model.maximize() : model.minimize();
        if(status != CLP.STATUS.OPTIMAL) {
            logger.debug("Optimal solution was not found. Was: [" + status + "]");
            return false;
        }

        var queue2 = new ArrayDeque<SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>>();
        queue2.addFirst(root);

        while(!queue2.isEmpty()) {
            var node = queue2.pop();

            var metadata = node.getSearchNodeMetadata();
            if(metadata.getNodeProbabilityFlow() == null) {
                metadata.setFlow(node.getParent().getSearchNodeMetadata().getFlow() * metadata.getPriorProbability());
            }
            metadata.afterSolution();
            queue2.addAll(node.getChildNodeMap().values());
        }

        if(root.getSearchNodeMetadata().getFlow() < FLOW_TOLERANCE) {
            throw new IllegalStateException("Flow is not equal to 1: [" + root.getSearchNodeMetadata().getFlow() + "]");
        }
        model.close();

        if(!root.getChildNodeMap().isEmpty()) {
            var sum = 0.0;
            for (var entry : root.getChildNodeMap().values()) {
                sum += entry.getSearchNodeMetadata().getFlow();
            }
            if(sum < FLOW_TOLERANCE) {
                throw new IllegalStateException("Flow is not equal to 1: [" + sum + "]. Root flow: [" + root.getSearchNodeMetadata().getFlow() + "]");
            }
        }
        if(TRACE_ENABLED) {
            logger.trace("Optimizing linear program took [{}] ms", System.currentTimeMillis() - startOptimization);
        }
        return true;
    }

    public void finalizeFlowCoefficients() {
        for (FlowWithCoefficient flowWithCoefficient : flowList) {
            model.setObjectiveCoefficient(flowWithCoefficient.getClosestParentFlow(), addNoiseToLeaf(flowWithCoefficient.getCoefficient()));
        }
    }

    protected final double getNodeValue(TSearchNodeMetadata metadata, int inGameEntityId) {
        double cumulativeReward = metadata.getCumulativeReward()[inGameEntityId];
        double expectedReward = metadata.getExpectedReward()[inGameEntityId];
        return addNoiseToLeaf(cumulativeReward + expectedReward);
    }

    protected final double addNoiseToLeaf(double leafCoefficient) {
        if (strategy != NoiseStrategy.NONE) {
            var value = random.nextDouble(noiseLowerBound, noiseUpperBound);
            leafCoefficient = leafCoefficient + (random.nextBoolean() ? value : -value);
        }
        return leafCoefficient;
    }

    private void initializeQueues(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> root) {
        root.getSearchNodeMetadata().setNodeProbabilityFlow(model.addVariable().lb(UPPER_BOUND).ub(UPPER_BOUND));
        var flow = new FlowWithCoefficient(root.getSearchNodeMetadata().getNodeProbabilityFlow());
        masterQueue.addFirst(new InnerElement<>(root, 1.0, flow));
        if(!root.isPlayerTurn()) {
            flowList.add(flow);
        }
    }

}
