package vahy.impl.search.nodeSelector.treeTraversing.ucb1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.node.nodeMetadata.MCTSNodeMetadata;
import vahy.impl.search.nodeSelector.AbstractTreeBasedNodeSelector;
import vahy.utils.StreamUtils;

import java.util.Comparator;
import java.util.Map;
import java.util.SplittableRandom;

public class Ucb1NodeSelector<
    TAction extends Action,
    TReward extends DoubleScalarReward,
    TObservation extends Observation,
    TState extends State<TAction, TReward, TObservation, TState>>
    extends AbstractTreeBasedNodeSelector<TAction, TReward, TObservation, MCTSNodeMetadata<TReward>, TState> {

    private final Logger logger = LoggerFactory.getLogger(Ucb1NodeSelector.class);

    protected SearchNode<TAction, TReward, TObservation, MCTSNodeMetadata<TReward>, TState> root;
    protected final SplittableRandom random;
    protected final double explorationConstant; // TODO: get rid of explorationConstant here. There are Ucb1 heuristics with changing exploration constant

    public Ucb1NodeSelector(SplittableRandom random, double explorationConstant) {
        this.random = random;
        this.explorationConstant = explorationConstant;
    }

    @Override
    protected TAction getBestAction(SearchNode<TAction, TReward, TObservation, MCTSNodeMetadata<TReward>, TState> node) {
        int nodeVisitCount = node.getSearchNodeMetadata().getVisitCounter();
        Map<TAction, SearchNode<TAction, TReward, TObservation, MCTSNodeMetadata<TReward>, TState>> childNodeMap = node.getChildNodeMap();
        return node.getChildNodeStream()
            .collect(StreamUtils.toRandomizedMaxCollector(
                Comparator.comparing(
                    o -> calculateUCBValue( // TODO: optimize so calls are done only once
                        childNodeMap.get(o.getAppliedAction()).getSearchNodeMetadata().getEstimatedTotalReward().getValue(),
                        explorationConstant,
                        nodeVisitCount,
                        o.getSearchNodeMetadata().getVisitCounter())),
                random))
            .getAppliedAction();
    }

    protected double calculateUCBValue(double estimatedValue, double explorationConstant, int parentVisitCount, int actionVisitCount) {
        return estimatedValue + explorationConstant * Math.sqrt(Math.log(parentVisitCount) / (1.0 + actionVisitCount));

    }
}
