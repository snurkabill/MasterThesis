package vahy.impl.search.simulation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.Observation;
import vahy.api.model.State;
import vahy.api.model.reward.Reward;
import vahy.api.model.reward.RewardAggregator;
import vahy.api.search.node.SearchNode;
import vahy.api.search.node.nodeMetadata.SearchNodeMetadata;
import vahy.api.search.node.nodeMetadata.StateActionMetadata;
import vahy.api.search.simulation.NodeEvaluationSimulator;
import vahy.timer.SimpleTimer;

import java.util.Map;

public abstract class AbstractNodeEvaluationSimulator<
    TAction extends Action,
    TReward extends Reward,
    TObservation extends Observation,
    TStateActionMetadata extends StateActionMetadata<TReward>,
    TSearchNodeMetadata extends SearchNodeMetadata<TAction, TReward, TStateActionMetadata>,
    TState extends State<TAction, TReward, TObservation>> implements NodeEvaluationSimulator<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNodeEvaluationSimulator.class);
    private final SimpleTimer timer = new SimpleTimer();

    protected final RewardAggregator<TReward> rewardAggregator;
    protected final double discountFactor;

    protected AbstractNodeEvaluationSimulator(RewardAggregator<TReward> rewardAggregator, double discountFactor) {
        this.rewardAggregator = rewardAggregator;
        this.discountFactor = discountFactor;
    }

    @Override
    public void calculateMetadataEstimation(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> expandedNode) {
        if(expandedNode.isFinalNode()) {
            throw new IllegalStateException("Final node cannot be evaluated anymore");
        }
        TSearchNodeMetadata searchNodeMetadata = expandedNode.getSearchNodeMetadata();
        for (Map.Entry<TAction, SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState>> entry : expandedNode.getChildNodeMap().entrySet()) {
            timer.startTimer();
            TReward expectedReward = calcExpectedReward(expandedNode, entry.getKey());
            entry.getValue().getSearchNodeMetadata().setEstimatedTotalReward(expectedReward);
            searchNodeMetadata.getStateActionMetadataMap().get(entry.getKey()).setEstimatedTotalReward(expectedReward);
            timer.stopTimer();
            logger.debug("Expected reward simulation for action [{}] calculated in [{}] seconds", entry.getKey(), timer.secondsSpent());
        }
        expandedNode.getSearchNodeMetadata().setEstimatedTotalReward(
            rewardAggregator.averageReward(searchNodeMetadata
                .getStateActionMetadataMap()
                .values()
                .stream()
                .map(StateActionMetadata::getEstimatedTotalReward)
            )
        );
    }

    protected abstract TReward calcExpectedReward(SearchNode<TAction, TReward, TObservation, TStateActionMetadata, TSearchNodeMetadata, TState> node, TAction firstAction);


}
