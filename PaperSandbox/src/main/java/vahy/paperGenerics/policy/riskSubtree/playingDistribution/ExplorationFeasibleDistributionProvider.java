package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.riskSubtree.SubtreeRiskCalculator;
import vahy.utils.RandomDistributionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.function.Supplier;

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
        int childCount = node.getChildNodeMap().size();
        List<TAction> actionList = new ArrayList<>(childCount);
        double[] distributionAsArray = new double[childCount];
        double[] riskArray = new double[childCount];

        int j = 0;
        for (Map.Entry<TAction, SearchNode<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState>> entry : node.getChildNodeMap().entrySet()) {
            actionList.add(entry.getKey());
            var metadata = entry.getValue().getSearchNodeMetadata();
            distributionAsArray[j] = metadata.getNodeProbabilityFlow().getSolution();
            var minimalRiskReachAbilityCalculator = distributionAsArray[j] - TOLERANCE <= 0.0
                ? subtreeRiskCalculatorSupplierForUnknownFlow.get()
                : subtreeRiskCalculatorSupplierForKnownFlow.get();
            riskArray[j] = minimalRiskReachAbilityCalculator.calculateRisk(entry.getValue());
            j++;
        }

        double[] originalDistributionAsArray = new double[distributionAsArray.length];
        System.arraycopy(distributionAsArray, 0, originalDistributionAsArray, 0, distributionAsArray.length);
        RandomDistributionUtils.tryToRoundDistribution(distributionAsArray);
        RandomDistributionUtils.applyBoltzmannNoise(distributionAsArray, temperature);

        var sum = 0.0d;
        for (int i = 0; i < distributionAsArray.length; i++) {
            sum += distributionAsArray[i] * riskArray[i];
        }
        if(sum > totalRiskAllowed) {

            for(double riskBound = totalRiskAllowed; riskBound <= 1.0; riskBound += 0.01) {
                var suitableExplorationDistribution = RandomDistributionUtils.findSimilarSuitableDistributionByLeastSquares(
                    distributionAsArray,
                    riskArray,
                    riskBound);
                if(suitableExplorationDistribution.getFirst()) {
                    int index = RandomDistributionUtils.getRandomIndexFromDistribution(suitableExplorationDistribution.getSecond(), random);
                    return new PlayingDistribution<>(
                        actionList.get(index),
                        index,
                        suitableExplorationDistribution.getSecond(),
                        riskArray,
                        actionList,
                        subtreeRiskCalculatorSupplierForUnknownFlow); // TODO TODO TODO TODO TODO FUCK THIS
                }
            }
            throw new IllegalStateException("Solution for linear risk-distribution optimisation was not found. Total risk allowed: [" + totalRiskAllowed +
                "] alternated probabilityDistribution: [" + Arrays.toString(distributionAsArray) +
                "] action risk array: [" + Arrays.toString(riskArray) +
                "] summed risk for original distribution with boltzmann noise: [" + sum +
                "] original probability array: [" + Arrays.toString(originalDistributionAsArray) +
                "] This is probably due to numeric inconsistency. Boltzmann exploration can have such effect with SOFT flow optimizer when allowed risk is 0.");
        } else {
            int index = RandomDistributionUtils.getRandomIndexFromDistribution(distributionAsArray, random);
            return new PlayingDistribution<>(
                actionList.get(index),
                index, distributionAsArray,
                riskArray,
                actionList, subtreeRiskCalculatorSupplierForUnknownFlow); // TODO TODO TODO TODO TODO FUCK THIS
        }
    }
}
