package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperMetadata;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.policy.riskSubtree.SubtreeRiskCalculator;
import vahy.utils.ImmutableTriple;
import vahy.utils.RandomDistributionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ExplorationFeasibleDistributionProvider<
    TAction extends Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractPlayingDistributionProvider<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> {

    private final Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplierForKnownFlow;
    private final Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplierForUnknownFlow;
    private final double totalRiskAllowed;
    private final double temperature;

    public ExplorationFeasibleDistributionProvider(List<TAction> playerActions,
                                                   SplittableRandom random,
                                                   Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplierForKnownFlow,
                                                   Supplier<SubtreeRiskCalculator<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplierForUnknownFlow,
                                                   double totalRiskAllowed,
                                                   double temperature) {
        super(playerActions, random);
        this.subtreeRiskCalculatorSupplierForKnownFlow = subtreeRiskCalculatorSupplierForKnownFlow;
        this.subtreeRiskCalculatorSupplierForUnknownFlow = subtreeRiskCalculatorSupplierForUnknownFlow;
        this.totalRiskAllowed  = totalRiskAllowed;
        this.temperature = temperature;
    }

    @Override
    public PlayingDistribution<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> createDistribution(
        SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> node) {
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
                subtreeRiskCalculatorSupplierForUnknownFlow); // TODO TODO TODO TODO TODO FUCK THIS
        }
    }
}
