package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.riskSubtree.SubtreeRiskCalculator;
import vahy.utils.ImmutableTriple;
import vahy.utils.RandomDistributionUtils;

import java.util.SplittableRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class InferenceFeasibleDistributionProvider<
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractPlayingDistributionProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private final Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplier;

    public InferenceFeasibleDistributionProvider(Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplier) {
        super(true);
        this.subtreeRiskCalculatorSupplier = subtreeRiskCalculatorSupplier;
    }

    @Override
    public PlayingDistribution<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> createDistribution(
        SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node,
        double temperature,
        SplittableRandom random,
        double totalRiskAllowed)
    {
        var alternateDistribution = createDistributionAsArray(node
            .getChildNodeStream()
            .map(x -> {
                var probabilityFlowFromGlobalOptimization = x.getSearchNodeMetadata().getNodeProbabilityFlow().getSolution();
                var subtreeRisk = probabilityFlowFromGlobalOptimization - TOLERANCE <= 0.0
                    ? 1.0
                    : subtreeRiskCalculatorSupplier.get().calculateRisk(x) / probabilityFlowFromGlobalOptimization;
                return new ImmutableTriple<>(x.getAppliedAction(), probabilityFlowFromGlobalOptimization, subtreeRisk);
            })
            .collect(Collectors.toList()));
        RandomDistributionUtils.tryToRoundDistribution(alternateDistribution.getSecond());
        int index = RandomDistributionUtils.getRandomIndexFromDistribution(alternateDistribution.getSecond(), random);
        return new PlayingDistribution<>(
            alternateDistribution.getFirst().get(index),
            index,
            alternateDistribution.getSecond(),
            alternateDistribution.getThird(),
            subtreeRiskCalculatorSupplier);
    }
}
