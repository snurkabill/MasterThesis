package vahy.impl.experiment;

import vahy.api.episode.EpisodeResults;
import vahy.api.experiment.AlgorithmConfig;
import vahy.api.experiment.Config;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;
import vahy.api.policy.PolicyRecord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EpisodeWriter<
    TAction extends Enum<TAction> & Action,
    TPlayerObservation extends Observation,
    TOpponentObservation extends Observation,
    TState extends State<TAction, TPlayerObservation, TOpponentObservation, TState>,
    TPolicyRecord extends PolicyRecord> {

    private final Path rootPath;
    private final Path fullPath;

    public EpisodeWriter(ProblemConfig problemConfig, AlgorithmConfig algorithmConfig, SystemConfig systemConfig, Path path, String timestamp, String policyName) {
        this.rootPath = path;

        File resultFolder = this.rootPath.toFile();
        if(!resultFolder.exists()) {
            checkFolderCreated(resultFolder, resultFolder.mkdir());
        }
        File resultSubFolder = Paths.get(resultFolder.getAbsolutePath(), timestamp).toFile();
        if(!resultSubFolder.exists()) {
            checkFolderCreated(resultSubFolder, resultSubFolder.mkdir());
        }

        var resultToFullPath = Paths.get(resultSubFolder.getAbsolutePath(), policyName).toFile();
        if(resultToFullPath.exists()) {
            throw new IllegalStateException("Policies have same name: [" + policyName + "]");
        }
        checkFolderCreated(resultToFullPath, resultToFullPath.mkdir());

        this.fullPath = resultToFullPath.toPath();

        printConfig(problemConfig, "ProblemConfig", resultToFullPath);
        printConfig(algorithmConfig, "AlgorithmConfig", resultToFullPath);
        printConfig(systemConfig, "SystemConfig", resultToFullPath);
    }

    private void printConfig(Config config, String configName, File resultSubfolder) {
        File experimentSetupFile = new File(resultSubfolder, configName);
        try {
            var out = new PrintWriter(experimentSetupFile);
            out.print(config.toFile());
            out.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkFolderCreated(File resultFolder, boolean created) {
        if(!created) {
            throw new RuntimeException("Unable to create results folder: [" + resultFolder.getAbsolutePath() + "]");
        }
    }

    public void writeTrainingEpisode(int stageId, List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> episodeResults)  {
        for (int i = 0; i < episodeResults.size(); i++) {
            Path path = Paths.get(fullPath.toString(), "training", "stageId_" + stageId, "episodeId_" + i);
            createDirAndWriteData(episodeResults.get(i), path);
        }
    }

    public void writeEvaluationEpisode(List<EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord>> episodeResultsList) {
        for (int i = 0; i < episodeResultsList.size(); i++) {
            Path path = Paths.get(fullPath.toString(),"evaluation", "episodeId_" + i);
            createDirAndWriteData(episodeResultsList.get(i), path);
        }
    }

    private void createDirAndWriteData(EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeResults, Path path) {
        File file = new File(path.toUri());
        if(!file.exists()) {
            checkFolderCreated(file, file.mkdirs());
        }
        var filename = file.getAbsolutePath();
        try {
            writeEpisodeMetadata(filename, episodeResults);
            BufferedWriter outputWriter = new BufferedWriter(new FileWriter(Paths.get(filename, "data").toFile()));
            outputWriter.write(String.join(",", episodeResults.getEpisodeHistory().get(0).getCsvHeader()) + System.lineSeparator());
            for (int i = 0; i < episodeResults.getEpisodeHistory().size(); i++) {
                outputWriter.write(String.join(",", episodeResults.getEpisodeHistory().get(i).getCsvRecord()) + System.lineSeparator());
            }
            outputWriter.flush();
            outputWriter.close();

            BufferedWriter outputWriterStates = new BufferedWriter(new FileWriter(filename + "/stateDump"));
            outputWriterStates.write(String.join(",", episodeResults.getEpisodeHistory().get(0).getFromState().getCsvHeader()) + System.lineSeparator());
            for (int i = 0; i < episodeResults.getEpisodeHistory().size(); i++) {
                outputWriterStates.write(String.join(",", episodeResults.getEpisodeHistory().get(i).getFromState().getCsvRecord()) + System.lineSeparator());
            }
            outputWriterStates.write(String.join(",", episodeResults.getEpisodeHistory().get(episodeResults.getEpisodeHistory().size() - 1).getToState().getCsvRecord()) + System.lineSeparator());
            outputWriterStates.flush();
            outputWriterStates.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void writeEpisodeMetadata(String filename, EpisodeResults<TAction, TPlayerObservation, TOpponentObservation, TState, TPolicyRecord> episodeResults) throws IOException {
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(filename + "/metadata"));
        outputWriter.write(episodeResults.episodeMetadataToFile());
        outputWriter.flush();
        outputWriter.close();
    }

}
