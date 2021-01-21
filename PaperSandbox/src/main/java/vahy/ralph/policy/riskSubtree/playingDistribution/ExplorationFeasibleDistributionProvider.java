package vahy.ralph.policy.riskSubtree.playingDistribution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.RiskState;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.search.node.SearchNode;
import vahy.ralph.metadata.RalphMetadata;
import vahy.ralph.policy.riskSubtree.SubtreeRiskCalculator;
import vahy.utils.RandomDistributionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.function.Supplier;

public class ExplorationFeasibleDistributionProvider<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TSearchNodeMetadata extends RalphMetadata<TAction>,
    TState extends RiskState<TAction, TObservation, TState>>
    extends AbstractPlayingDistributionProvider<TAction, TObservation, TSearchNodeMetadata, TState> {

    private static final Logger logger = LoggerFactory.getLogger(ExplorationFeasibleDistributionProvider.class.getName());

    private static final double RISK_BOUND_DELTA = 0.01;

    private final Class<TAction> clazz;

    private final Supplier<SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplier;

    public ExplorationFeasibleDistributionProvider(Class<TAction> clazz, Supplier<SubtreeRiskCalculator<TAction, TObservation, TSearchNodeMetadata, TState>> subtreeRiskCalculatorSupplier) {
        super(true);
        this.clazz = clazz;
        this.subtreeRiskCalculatorSupplier = subtreeRiskCalculatorSupplier;
    }

    @Override
    public PlayingDistributionWithActionMap<TAction> createDistribution(SearchNode<TAction, TObservation, TSearchNodeMetadata, TState> node, double temperature, SplittableRandom random, double totalRiskAllowed)
    {
        int inGameEntityId = node.getStateWrapper().getInGameEntityId();
        var childMap = node.getChildNodeMap();
        var childCount = childMap.size();
        var actionList = new ArrayList<TAction>(childCount);
        var distributionAsArray = new double[childCount];
        var riskArray = new double[childCount];

        var j = 0;
        for (Map.Entry<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> entry : node.getChildNodeMap().entrySet()) {
            actionList.add(entry.getKey());
            var metadata = entry.getValue().getSearchNodeMetadata();
            distributionAsArray[j] = metadata.getFlow();
            riskArray[j] = subtreeRiskCalculatorSupplier.get().calculateRisk(entry.getValue());
            j++;
        }

        RandomDistributionUtils.tryToRoundDistribution(distributionAsArray, TOLERANCE);
        RandomDistributionUtils.applyBoltzmannNoise(distributionAsArray, temperature);

//        var anyActionViolatesRisk = false;
//        for (int i = 0; i < distributionAsArray.length; i++) {
//            var product = distributionAsArray[i] * riskArray[i];
//            if(product > totalRiskAllowed) {
//                anyActionViolatesRisk = true;
//                break;
//            }
//        }
//        if(anyActionViolatesRisk) {

        var sum = 0.0d;
        for (int i = 0; i < distributionAsArray.length; i++) {
            sum += distributionAsArray[i] * riskArray[i];
        }

        var anyActionViolatesRisk = false;
        for (int i = 0; i < distributionAsArray.length; i++) {
            var product = distributionAsArray[i] * riskArray[i];
            if(product > totalRiskAllowed) {
                anyActionViolatesRisk = true;
                break;
            }
        }

        var isDefinitelyOK = !anyActionViolatesRisk && sum <= totalRiskAllowed;
        var isDefinitelyWrong = anyActionViolatesRisk && sum > totalRiskAllowed;

        var isSumStrict = sum > totalRiskAllowed && !anyActionViolatesRisk;
        var isViolationStrict = anyActionViolatesRisk && sum <= totalRiskAllowed;


        logger.debug("Is ok: [{}], is sum strict: [{}], isViolationStrict: [{}], isDefinitelyWrong: [{}]", isDefinitelyOK, isSumStrict, isViolationStrict, isDefinitelyWrong);

        if(sum > totalRiskAllowed) {

            for(double riskBound = totalRiskAllowed; riskBound <= 1.0; riskBound += RISK_BOUND_DELTA) {
                var suitableExplorationDistribution = RandomDistributionUtils.findSimilarSuitableDistributionByLeastSquares(
                    distributionAsArray,
                    riskArray,
                    riskBound);
                if(suitableExplorationDistribution.getFirst()) {
                    int index = RandomDistributionUtils.getRandomIndexFromDistribution(suitableExplorationDistribution.getSecond(), random);
                    return getActionPlayingDistributionWithWithActionMap(inGameEntityId, childMap, actionList, suitableExplorationDistribution.getSecond(), index);
                }
            }
            throw new IllegalStateException("Solution for linear risk-distribution optimisation was not found. Total risk allowed: [" + totalRiskAllowed +
                "] alternated probabilityDistribution: [" + Arrays.toString(distributionAsArray) +
                "] action risk array: [" + Arrays.toString(riskArray) +
                "] summed risk for original distribution with boltzmann noise: [" + sum +
                "] This is probably due to numeric inconsistency. Boltzmann exploration can have such effect with SOFT flow optimizer when allowed risk is 0.");
        } else {
            int index = RandomDistributionUtils.getRandomIndexFromDistribution(distributionAsArray, random);
            return getActionPlayingDistributionWithWithActionMap(inGameEntityId, childMap, actionList, distributionAsArray, index);
        }
    }

    private PlayingDistributionWithActionMap<TAction> getActionPlayingDistributionWithWithActionMap(int inGameEntityId,
                                                                                                    Map<TAction, SearchNode<TAction, TObservation, TSearchNodeMetadata, TState>> childMap,
                                                                                                    ArrayList<TAction> actionList,
                                                                                                    double[] distributionAsArray,
                                                                                                    int index)
    {
        var action = actionList.get(index);
        EnumMap<TAction, Double> enumMap = new EnumMap<>(clazz);
        for (int i = 0; i < actionList.size(); i++) { // TODO: sample action right away from enum map
            enumMap.put(actionList.get(i), distributionAsArray[i]);
        }
        return new PlayingDistributionWithActionMap<>(action, childMap.get(action).getSearchNodeMetadata().getExpectedReward()[inGameEntityId], distributionAsArray, enumMap);
    }
}
