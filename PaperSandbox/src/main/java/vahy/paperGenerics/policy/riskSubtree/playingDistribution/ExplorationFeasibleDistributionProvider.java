package vahy.paperGenerics.policy.riskSubtree.playingDistribution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.riskSubtree.SubtreeRiskCalculator;
import vahy.utils.RandomDistributionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class ExplorationFeasibleDistributionProvider<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TObservation, TState>>
    extends AbstractPlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(ExplorationFeasibleDistributionProvider.class.getName());
    private static final boolean TRACE_ENABLED = logger.isTraceEnabled();
    private static final boolean DEBUG_ENABLED = logger.isDebugEnabled();
    private static final double RISK_BOUND_DELTA = 0.01;

    private final Supplier<SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplierForKnownFlow;
    private final Supplier<SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplierForUnknownFlow;

    public ExplorationFeasibleDistributionProvider(Supplier<SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplierForKnownFlow,
                                                   Supplier<SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplierForUnknownFlow) {
        super(true, null);
        this.subtreeRiskCalculatorSupplierForKnownFlow = subtreeRiskCalculatorSupplierForKnownFlow;
        this.subtreeRiskCalculatorSupplierForUnknownFlow = subtreeRiskCalculatorSupplierForUnknownFlow;
    }

    @Override
    public PlayingDistributionWithRisk<TAction, TObservation, TSearchNodeMetadata, TState> createDistribution(
        SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node,
        double temperature,
        SplittableRandom random,
        double totalRiskAllowed)
    {
        var childMap = node.getChildNodeMap();
        var childCount = childMap.size();
        var actionList = new ArrayList<TAction>(childCount);
        var riskSupplierMap = new HashMap<TAction, Supplier<SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState>>>(childCount);
        var distributionAsArray = new double[childCount];
        var riskArray = new double[childCount];

        var j = 0;
        for (Map.Entry<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> entry : node.getChildNodeMap().entrySet()) {
            actionList.add(entry.getKey());
            var metadata = entry.getValue().getSearchNodeMetadata();
            distributionAsArray[j] = metadata.getFlow();
            var riskSupplier = distributionAsArray[j] - TOLERANCE <= 0.0 ? subtreeRiskCalculatorSupplierForUnknownFlow : subtreeRiskCalculatorSupplierForKnownFlow;
            riskSupplierMap.put(entry.getKey(), riskSupplier);
            var minimalRiskReachAbilityCalculator = riskSupplier.get();

            if(DEBUG_ENABLED) {
                logger.debug("Calculating risk using [{}] risk calculator", minimalRiskReachAbilityCalculator.toLog());
            }
            riskArray[j] = minimalRiskReachAbilityCalculator.calculateRisk(entry.getValue());
            j++;
        }

        double[] originalDistributionAsArray = new double[distributionAsArray.length];
        System.arraycopy(distributionAsArray, 0, originalDistributionAsArray, 0, distributionAsArray.length);
        RandomDistributionUtils.tryToRoundDistribution(distributionAsArray, TOLERANCE);
        RandomDistributionUtils.applyBoltzmannNoise(distributionAsArray, temperature);

        var sum = 0.0d;
        for (int i = 0; i < distributionAsArray.length; i++) {
            sum += distributionAsArray[i] * riskArray[i];
        }
        if(sum > totalRiskAllowed) {

            for(double riskBound = totalRiskAllowed; riskBound <= 1.0; riskBound += RISK_BOUND_DELTA) {
                var suitableExplorationDistribution = RandomDistributionUtils.findSimilarSuitableDistributionByLeastSquares(
                    distributionAsArray,
                    riskArray,
                    riskBound);
                if(suitableExplorationDistribution.getFirst()) {
                    int index = RandomDistributionUtils.getRandomIndexFromDistribution(suitableExplorationDistribution.getSecond(), random);
                    return new PlayingDistributionWithRisk<>(actionList.get(index), index, suitableExplorationDistribution.getSecond(), riskArray, actionList, riskSupplierMap);
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
            return new PlayingDistributionWithRisk<>(actionList.get(index), index, distributionAsArray, riskArray, actionList, riskSupplierMap);
        }
    }
}
