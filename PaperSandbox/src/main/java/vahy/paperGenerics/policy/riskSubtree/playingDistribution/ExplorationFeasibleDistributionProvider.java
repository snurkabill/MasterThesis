package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.riskSubtree.SubtreeRiskCalculator;
import vahy.utils.ImmutableTriple;
import vahy.utils.RandomDistributionUtils;

import java.util.Arrays;
import java.util.SplittableRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ExplorationFeasibleDistributionProvider<
    TAction extends Action<TAction>,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractPlayingDistributionProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private final Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplierForKnownFlow;
    private final Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplierForUnknownFlow;

    public ExplorationFeasibleDistributionProvider(Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplierForKnownFlow,
                                                   Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplierForUnknownFlow) {
        super(true);
        this.subtreeRiskCalculatorSupplierForKnownFlow = subtreeRiskCalculatorSupplierForKnownFlow;
        this.subtreeRiskCalculatorSupplierForUnknownFlow = subtreeRiskCalculatorSupplierForUnknownFlow;
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
                var minimalRiskReachAbilityCalculator = probabilityFlowFromGlobalOptimization - TOLERANCE <= 0.0
                    ? subtreeRiskCalculatorSupplierForUnknownFlow.get()
                    : subtreeRiskCalculatorSupplierForKnownFlow.get();
                var subtreeRisk = minimalRiskReachAbilityCalculator.calculateRisk(x);
                return new ImmutableTriple<>(x.getAppliedAction(), probabilityFlowFromGlobalOptimization, subtreeRisk);
            })
            .collect(Collectors.toList()));

        var actionList = alternateDistribution.getFirst();
        double[] actionDistributionAsArray = alternateDistribution.getSecond();
        double[] originalDistributionAsArray = new double[actionDistributionAsArray.length];
        System.arraycopy(actionDistributionAsArray, 0, originalDistributionAsArray, 0, actionDistributionAsArray.length);
        RandomDistributionUtils.tryToRoundDistribution(actionDistributionAsArray);
        RandomDistributionUtils.applyBoltzmannNoise(actionDistributionAsArray, temperature);
        double[] actionRiskAsArray = alternateDistribution.getThird();

        var sum = 0.0d;
        for (int i = 0; i < actionDistributionAsArray.length; i++) {
            sum += actionDistributionAsArray[i] * actionRiskAsArray[i];
        }
        if(sum > totalRiskAllowed) {

            for(double riskBound = totalRiskAllowed; riskBound <= 1.0; riskBound += 0.01) {
                var suitableExplorationDistribution = RandomDistributionUtils.findSimilarSuitableDistributionByLeastSquares(
                    actionDistributionAsArray,
                    alternateDistribution.getThird(),
                    riskBound);
                if(suitableExplorationDistribution.getFirst()) {
                    int index = RandomDistributionUtils.getRandomIndexFromDistribution(suitableExplorationDistribution.getSecond(), random);
                    return new PlayingDistribution<>(
                        alternateDistribution.getFirst().get(index),
                        index,
                        suitableExplorationDistribution.getSecond(),
                        alternateDistribution.getThird(),
                        actionList,
                        subtreeRiskCalculatorSupplierForUnknownFlow); // TODO TODO TODO TODO TODO FUCK THIS
                }
            }
            throw new IllegalStateException("Solution for linear risk-distribution optimisation was not found. Total risk allowed: [" + totalRiskAllowed +
                "] alternated probabilityDistribution: [" + Arrays.toString(actionDistributionAsArray) +
                "] action risk array: [" + Arrays.toString(actionRiskAsArray) +
                "] summed risk for original distribution with boltzmann noise: [" + sum +
                "] original probability array: [" + Arrays.toString(originalDistributionAsArray) +
                "] This is probably due to numeric inconsistency. Boltzmann exploration can have such effect with SOFT flow optimizer when allowed risk is 0.");
        } else {
            int index = RandomDistributionUtils.getRandomIndexFromDistribution(actionDistributionAsArray, random);
            return new PlayingDistribution<>(
                alternateDistribution.getFirst().get(index),
                index, actionDistributionAsArray,
                alternateDistribution.getThird(),
                actionList, subtreeRiskCalculatorSupplierForUnknownFlow); // TODO TODO TODO TODO TODO FUCK THIS
        }
    }
}
