package vahy.integration.tictactoe;

import vahy.AbstractConvergenceTest;
import vahy.api.experiment.CommonAlgorithmConfigBase;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.StateWrapper;
import vahy.examples.tictactoe.AlwaysStartAtCornerPolicy;
import vahy.examples.tictactoe.AlwaysStartAtMiddlePolicy;
import vahy.examples.tictactoe.TicTacToeAction;
import vahy.examples.tictactoe.TicTacToeConfig;
import vahy.examples.tictactoe.TicTacToeState;
import vahy.examples.tictactoe.TicTacToeStateInitializer;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.EpisodeStatisticsBase;
import vahy.impl.benchmark.EpisodeStatisticsCalculatorBase;
import vahy.impl.episode.EpisodeResultsFactoryBase;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.UniformRandomWalkPolicy;
import vahy.impl.runner.PolicyDefinition;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTicTacToeConvergenceTest extends AbstractConvergenceTest {


    protected static PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState> createUniformPolicy(int policyId_) {
        return new PolicyDefinition<>(
            policyId_,
            1,
            (initialState, policyMode, policyId, random) -> new UniformRandomWalkPolicy<>(random, policyId),
            new ArrayList<>(0)
        );
    }

    protected static PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState> createAtMiddlePolicy(int policyId_) {
        return new PolicyDefinition<>(
            policyId_,
            1,
            (initialState, policyMode, policyId, random) -> new AlwaysStartAtMiddlePolicy(random, policyId),
            new ArrayList<>(0)
        );
    }

    protected static PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState> createAtCornerPolicy(int policyId_) {
        return new PolicyDefinition<>(
            policyId_,
            1,
            (initialState, policyMode, policyId, random) -> new AlwaysStartAtCornerPolicy(random, policyId),
            new ArrayList<>(0)
        );
    }



    protected RoundBuilder<TicTacToeConfig, TicTacToeAction, TicTacToeState, EpisodeStatisticsBase> getRoundBuilder(TicTacToeConfig ticTacConfig,
                                                                                                                    SystemConfig systemConfig,
                                                                                                                    CommonAlgorithmConfigBase algorithmConfig,
                                                                                                                    List<PolicyDefinition<TicTacToeAction, DoubleVector, TicTacToeState>> policyArgumentsList) {
        return new RoundBuilder<TicTacToeConfig, TicTacToeAction, TicTacToeState, EpisodeStatisticsBase>()
            .setRoundName("TicTacToeIntegrationTest")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(algorithmConfig)
            .setProblemConfig(ticTacConfig)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((config_, splittableRandom_) -> policyMode -> new TicTacToeStateInitializer(config_, splittableRandom_).createInitialState(policyMode))
            .setStateStateWrapperInitializer(StateWrapper::new)
            .setResultsFactory(new EpisodeResultsFactoryBase<>())
            .setStatisticsCalculator(new EpisodeStatisticsCalculatorBase<>())
            .setPlayerPolicySupplierList(policyArgumentsList);
    }

}
