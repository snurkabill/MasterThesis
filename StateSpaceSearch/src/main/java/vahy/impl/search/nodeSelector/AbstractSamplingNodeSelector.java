package vahy.impl.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.NodeMetadata;
import vahy.api.search.node.ProbabilisticNodeMetadata;
import vahy.api.search.node.SearchNode;
import vahy.api.search.nodeSelector.RandomizedNodeSelector;
import vahy.utils.RandomDistributionUtils;

import java.util.EnumMap;
import java.util.SplittableRandom;

public abstract class AbstractSamplingNodeSelector<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends NodeMetadata & ProbabilisticNodeMetadata<TAction>,
    TState extends State<TAction, TObservation, TState>>
    extends RandomizedNodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> {

    public AbstractSamplingNodeSelector(SplittableRandom random) {
        super(random);
    }

    protected final TAction sampleAction(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node) {
        EnumMap<TAction, Double> map = node.getSearchNodeMetadata().getChildPriorProbabilities();
        TAction action = RandomDistributionUtils.getRandomElementFromMapDistribution(map, random);
        if(TRACE_ENABLED) {
            logger.trace("Sampled [{}] action from opponent's actions: [{}].", action, map.toString());
        }
        return action;
    }

}
