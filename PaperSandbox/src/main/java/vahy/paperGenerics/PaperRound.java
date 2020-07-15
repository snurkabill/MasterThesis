package vahy.paperGenerics;

import vahy.api.episode.InitialStateSupplier;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.Action;
import vahy.impl.RoundBuilder;
import vahy.impl.benchmark.PolicyResults;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.runner.PolicyDefinition;
import vahy.paperGenerics.benchmark.PaperEpisodeStatistics;
import vahy.paperGenerics.benchmark.PaperEpisodeStatisticsCalculator;
import vahy.paperGenerics.policy.PaperPolicyRecord;
import vahy.paperGenerics.reinforcement.episode.PaperEpisodeResultsFactory;

import java.util.List;
import java.util.SplittableRandom;
import java.util.function.BiFunction;

public class PaperRound<TConfig extends ProblemConfig, TAction extends Enum<TAction> & Action, TState extends PaperState<TAction, DoubleVector, TState>> {

    private TConfig problemConfig;
    private SystemConfig systemConfig;
    private CommonAlgorithmConfig commonAlgorithmConfig;
    private BiFunction<TConfig, SplittableRandom, InitialStateSupplier<TAction, DoubleVector, TState>> initialStateSupplier;

    private List<PolicyDefinition<TAction, DoubleVector, TState>> policyDefinitionList;

    public PaperRound<TConfig, TAction, TState> setProblemConfig(TConfig problemConfig) {
        this.problemConfig = problemConfig;
        return this;
    }

    public PaperRound<TConfig, TAction, TState> setSystemConfig(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
        return this;
    }

    public PaperRound<TConfig, TAction, TState> setCommonAlgorithmConfig(CommonAlgorithmConfig commonAlgorithmConfig) {
        this.commonAlgorithmConfig = commonAlgorithmConfig;
        return this;
    }

    public PaperRound<TConfig, TAction, TState> setInitialStateSupplier(BiFunction<TConfig, SplittableRandom, InitialStateSupplier<TAction, DoubleVector, TState>> initialStateSupplier) {
        this.initialStateSupplier = initialStateSupplier;
        return this;
    }

    public PaperRound<TConfig, TAction, TState> setPolicyDefinitionList(List<PolicyDefinition<TAction, DoubleVector, TState>> policyDefinitionList) {
        this.policyDefinitionList = policyDefinitionList;
        return this;
    }

    private void finalizeSetup() {
        if(problemConfig == null) {
            throw new IllegalArgumentException("Missing problem config");
        }
        if(systemConfig == null) {
            throw new IllegalArgumentException("Missing system config");
        }
        if(commonAlgorithmConfig == null) {
            throw new IllegalArgumentException("Missing commonAlgorithm config");
        }
        if(initialStateSupplier == null) {
            throw new IllegalArgumentException("Missing initialStateSupplier");
        }
        if(policyDefinitionList == null) {
            throw new IllegalArgumentException("Missing policy definition list");
        }
        if(policyDefinitionList.isEmpty()) {
            throw new IllegalArgumentException("Policy definition list is empty.");
        }
    }

    public PolicyResults<TAction, DoubleVector, TState, PaperEpisodeStatistics> execute() {
        finalizeSetup();
        var roundBuilder = new RoundBuilder<TConfig, TAction, TState, PaperEpisodeStatistics>()
            .setRoundName("Idk")
            .setAdditionalDataPointGeneratorListSupplier(null)
            .setCommonAlgorithmConfig(commonAlgorithmConfig)
            .setProblemConfig(problemConfig)
            .setSystemConfig(systemConfig)
            .setProblemInstanceInitializerSupplier((config, random) -> policyMode -> initialStateSupplier.apply(config, random).createInitialState(policyMode))
            .setResultsFactory(new PaperEpisodeResultsFactory<>())
            .setStatisticsCalculator(new PaperEpisodeStatisticsCalculator<>())
            .setPlayerPolicySupplierList(policyDefinitionList);
        return roundBuilder.execute();
    }

}
