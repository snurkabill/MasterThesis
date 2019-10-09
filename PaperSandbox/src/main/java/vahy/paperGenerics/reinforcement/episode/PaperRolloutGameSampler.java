package vahy.paperGenerics.reinforcement.episode;

import vahy.api.episode.EpisodeResults;
import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.policy.Policy;
import vahy.api.policy.PolicyMode;
import vahy.impl.episode.FromEpisodesDataPointGeneratorGeneric;
import vahy.impl.learning.trainer.AbstractGameSamplerGeneric;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.PaperPolicyRecord;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.utils.MathStreamUtils;
import vahy.vizualiation.ProgressTrackerSettings;

import java.util.ArrayList;

public class PaperRolloutGameSampler<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends AbstractGameSamplerGeneric<TAction, TPlayerObservation, TOpponentObservation, TState, PaperPolicyRecord> {

    private final PaperPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> playerPolicySupplier;
    private final PaperPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> opponentPolicySupplier;

    public PaperRolloutGameSampler(InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                                   EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, PaperPolicyRecord> resultsFactory,
                                   PaperPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> playerPolicySupplier,
                                   PaperPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> opponentPolicySupplier,
                                   PolicyMode policyMode,
                                   ProgressTrackerSettings progressTrackerSettings,
                                   int processingUnitCount) {
        super(initialStateSupplier, resultsFactory, policyMode, progressTrackerSettings, processingUnitCount);
        this.playerPolicySupplier = playerPolicySupplier;
        this.opponentPolicySupplier = opponentPolicySupplier;

        createDataGenerators();
    }

    private void createDataGenerators() {
        var dataPointGeneratorList = new ArrayList<FromEpisodesDataPointGeneratorGeneric<TAction, TPlayerObservation, TOpponentObservation, TState, PaperPolicyRecord>>();

        dataPointGeneratorList.add(new FromEpisodesDataPointGeneratorGeneric<>(
            "Avg Player Step Count",
            episodeResults -> MathStreamUtils.calculateAverage(episodeResults, EpisodeResults::getPlayerStepCount)));

        dataPointGeneratorList.add(new FromEpisodesDataPointGeneratorGeneric<>(
            "Avg Total Payoff",
            episodeResults -> MathStreamUtils.calculateAverage(episodeResults, EpisodeResults::getTotalPayoff)));

        dataPointGeneratorList.add(new FromEpisodesDataPointGeneratorGeneric<>(
            "Avt risk ratio",
            episodeResults -> MathStreamUtils.calculateAverage(episodeResults, x -> x.getFinalState().isRiskHit() ? 1.0 : 0.0)));

        dataPointGeneratorList.add(new FromEpisodesDataPointGeneratorGeneric<>(
            "Avg episode duration [ms]",
            episodeResults -> MathStreamUtils.calculateAverage(episodeResults, (x) -> x.getDuration().toMillis())));

        registerDataGenerators(dataPointGeneratorList);
    }

    @Override
    protected Policy<TAction, TPlayerObservation, TOpponentObservation, TState, PaperPolicyRecord> supplyPlayerPolicy(TState initialState, PolicyMode policyMode) {
        return playerPolicySupplier.initializePolicy(initialState, policyMode);
    }

    @Override
    protected Policy<TAction, TPlayerObservation, TOpponentObservation, TState, PaperPolicyRecord> supplyOpponentPolicy(TState initialState, PolicyMode policyMode) {
        return opponentPolicySupplier.initializePolicy(initialState, policyMode);
    }

}
