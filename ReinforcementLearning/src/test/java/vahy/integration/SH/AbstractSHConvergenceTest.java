package vahy.integration.SH;

import vahy.api.benchmark.EpisodeStatistics;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.StateWrapper;
import vahy.examples.simplifiedHallway.SHAction;
import vahy.examples.simplifiedHallway.SHConfig;
import vahy.examples.simplifiedHallway.SHInstanceSupplier;
import vahy.examples.simplifiedHallway.SHState;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.EpisodeStatisticsCalculatorBase;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.runner.PolicyDefinition;

import java.util.List;

public abstract class AbstractSHConvergenceTest {

    protected RoundBuilder<SHConfig, SHAction, SHState, EpisodeStatistics> getRoundBuilder(SHConfig config,
                                                                                               CommonAlgorithmConfigBase algorithmConfig,
                                                                                               SystemConfig systemConfig,
                                                                                               PolicyDefinition<SHAction, DoubleVector, SHState> policyArgument) {
        return new RoundBuilder<SHConfig, SHAction, SHState, EpisodeStatistics>()
            .setRoundName("SHTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((config_, splittableRandom_) -> policyMode -> new SHInstanceSupplier(config_, splittableRandom_).createInitialState(policyMode))
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setStateStateWrapperInitializer(StateWrapper::new)
            .setPlayerPolicySupplierList(List.of(policyArgument));
    }

}
