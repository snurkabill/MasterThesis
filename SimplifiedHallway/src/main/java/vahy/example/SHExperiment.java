package vahy.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.experiment.SystemConfig;
import vahy.config.PaperAlgorithmConfig;
import vahy.domain.SHAction;
import vahy.domain.SHConfig;
import vahy.domain.SHInstanceSupplier;
import vahy.domain.SHState;
import vahy.impl.benchmark.PolicyResults;
import vahy.impl.model.observation.DoubleVector;
import vahy.impl.policy.KnownModelPolicySupplier;
import vahy.paperGenerics.PaperExperimentBuilder;
import vahy.paperGenerics.benchmark.PaperEpisodeStatistics;
import vahy.paperGenerics.policy.PaperPolicyRecord;
import vahy.utils.ThirdPartBinaryUtils;

import java.util.List;

public abstract class SHExperiment {

    public static final Logger logger = LoggerFactory.getLogger(SHExperiment.class.getName());

    public void runBenchmark() {
        ThirdPartBinaryUtils.cleanUpNativeTempFiles();

        var algorithmConfigList = createAlgorithmConfigList();
        var systemConfig = createSystemConfig();
        var problemConfig = createProblemConfig();

        var paperExperimentBuilder = new PaperExperimentBuilder<SHConfig, SHAction, SHState, SHState>()
            .setActionClass(SHAction.class)
            .setStateClass(SHState.class)
            .setSystemConfig(systemConfig)
            .setAlgorithmConfigList(algorithmConfigList)
            .setProblemConfig(problemConfig)
            .setProblemInstanceInitializerSupplier(SHInstanceSupplier::new)
            .setOpponentSupplier(KnownModelPolicySupplier::new);

        var results = paperExperimentBuilder.execute();

        for (PolicyResults<SHAction, DoubleVector, SHState, SHState, PaperPolicyRecord, PaperEpisodeStatistics> result : results) {
            logger.info("PolicyId: " + result.getPolicyList().getPolicyName());
            logger.info("Results: " + result.getEvaluationStatistics().printToLog());
        }
    }

    protected abstract SHConfig createProblemConfig();

    protected abstract SystemConfig createSystemConfig();

    protected abstract List<PaperAlgorithmConfig> createAlgorithmConfigList();


}
