package vahy.integration.SH;

import vahy.AbstractConvergenceTest;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.StateWrapper;
import vahy.examples.simplifiedHallway.SHAction;
import vahy.examples.simplifiedHallway.SHConfig;
import vahy.examples.simplifiedHallway.SHInstanceSupplier;
import vahy.examples.simplifiedHallway.SHState;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.EpisodeStatisticsCalculatorBase;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.runner.PolicyDefinition;

import java.util.List;

public abstract class AbstractSHConvergenceTest extends AbstractConvergenceTest {

    protected RoundBuilder<SHConfig, SHAction, SHState, EpisodeStatisticsBase> getRoundBuilder(SHConfig config,
                                                                                               CommonAlgorithmConfigBase algorithmConfig,
                                                                                               SystemConfig systemConfig,
                                                                                               PolicyDefinition<SHAction, DoubleVector, SHState> policyArgument) {
        return new RoundBuilder<SHConfig, SHAction, SHState, EpisodeStatisticsBase>()
            .setRoundName("SHTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(config)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((SHConfig, splittableRandom) -> policyMode -> (new SHInstanceSupplier(config, splittableRandom)).createInitialState(policyMode))
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setStateStateWrapperInitializer(StateWrapper::new)
            .setPlayerPolicySupplierList(List.of(policyArgument));
    }

}
