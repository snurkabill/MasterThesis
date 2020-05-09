package vahy.paperGenerics.policy.flowOptimizer;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.linearProgram.NoiseStrategy;
import vahy.utils.ImmutableTuple;

import java.util.SplittableRandom;

public abstract class AbstractFlowOptimizer<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>> {

    protected final SplittableRandom random;
    protected final NoiseStrategy noiseStrategy;

    protected AbstractFlowOptimizer(SplittableRandom random, NoiseStrategy noiseStrategy) {
        this.random = random;
        this.noiseStrategy = noiseStrategy;
    }

    public abstract ImmutableTuple<Double, Boolean> optimizeFlow(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node, double totalRiskAllowed);

}
