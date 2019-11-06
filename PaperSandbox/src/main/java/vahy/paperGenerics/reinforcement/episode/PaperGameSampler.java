package vahy.paperGenerics.reinforcement.episode;

import vahy.api.episode.EpisodeResultsFactory;
import vahy.api.episode.InitialStateSupplier;
import vahy.api.model.Action;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyMode;
import vahy.impl.episode.FromEpisodesDataPointGeneratorGeneric;
import vahy.impl.learning.trainer.GameSamplerImpl;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.PaperState;
import vahy.paperGenerics.metadata.PaperMetadata;
import vahy.paperGenerics.policy.PaperPolicyRecord;
import vahy.paperGenerics.policy.PaperPolicySupplier;
import vahy.utils.MathStreamUtils;
import vahy.vizualiation.ProgressTrackerSettings;

import java.util.ArrayList;

public class PaperGameSampler<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends DoubleVector,
    TOpponentObservation extends Observation,
    TSearchNodeMetadata extends PaperMetadata<TAction>,
    TState extends PaperState<TAction, TPlayerObservation, TOpponentObservation, TState>>
    extends GameSamplerImpl<TAction, TPlayerObservation, TOpponentObservation, TState, PaperPolicyRecord> {

    public PaperGameSampler(InitialStateSupplier<TAction, TPlayerObservation, TOpponentObservation, TState> initialStateSupplier,
                            EpisodeResultsFactory<TAction, TPlayerObservation, TOpponentObservation, TState, PaperPolicyRecord> resultsFactory,
                            PaperPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> playerPolicySupplier,
                            PaperPolicySupplier<TAction, TPlayerObservation, TOpponentObservation, TSearchNodeMetadata, TState> opponentPolicySupplier,
                            PolicyMode policyMode,
                            ProgressTrackerSettings progressTrackerSettings,
                            int processingUnitCount) {
        super(initialStateSupplier, resultsFactory, policyMode, progressTrackerSettings, processingUnitCount, playerPolicySupplier, opponentPolicySupplier);
        createDataGenerators();
    }

    private void createDataGenerators() {
        var dataPointGeneratorList = new ArrayList<FromEpisodesDataPointGeneratorGeneric<TAction, TPlayerObservation, TOpponentObservation, TState, PaperPolicyRecord>>();
        dataPointGeneratorList.add(new FromEpisodesDataPointGeneratorGeneric<>(
            "Avg risk ratio",
            episodeResults -> MathStreamUtils.calculateAverage(episodeResults, x -> x.getFinalState().isRiskHit() ? 1.0 : 0.0)));
        registerDataGenerators(dataPointGeneratorList);
    }

}
