package vahy.impl.search.nodeSelector;

import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.predictor.Predictor;
import vahy.api.search.node.NodeMetadata;
import vahy.api.search.node.SearchNode;
import vahy.api.search.nodeSelector.RandomizedNodeSelector;
import vahy.utils.RandomDistributionUtils;

import java.util.SplittableRandom;

public abstract class EnvironmentSamplingNodeSelector<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends NodeMetadata,
    TState extends State<TAction, TObservation, TState>>
    extends RandomizedNodeSelector<TAction, TObservation, TSearchNodeMetadata, TState> {

    private Predictor<TState> perfectPredictor;

    public EnvironmentSamplingNodeSelector(SplittableRandom random) {
        super(random);
    }

    protected final TAction sampleAction(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node) {
        TState wrappedState = node.getStateWrapper().getWrappedState();
        if(perfectPredictor == null) {
            perfectPredictor = wrappedState.getKnownModelWithPerfectObservationPredictor();
        }
        int index = RandomDistributionUtils.getRandomIndexFromDistribution(perfectPredictor.apply(wrappedState), random);
        TAction action = node.getAllPossibleActions()[index];
        if(TRACE_ENABLED) {
            logger.trace("Sampled [{}] action from opponent's actions", action);
        }

//        EnumMap<TAction, Double> map = node.getSearchNodeMetadata().getChildPriorProbabilities();
//        TAction action = RandomDistributionUtils.getRandomElementFromMapDistribution(map, random);
//        if(TRACE_ENABLED) {
//            logger.trace("Sampled [{}] action from opponent's actions: [{}].", action, map.toString());
//        }
        return action;
    }

    protected abstract TAction getBestAction_inner(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node);

    protected final TAction getBestAction(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node) {
        if(node.getStateWrapper().isEnvironmentEntityOnTurn()) {
            return sampleAction(node);
        } else {
            return getBestAction_inner(node);
        }
    }

}
