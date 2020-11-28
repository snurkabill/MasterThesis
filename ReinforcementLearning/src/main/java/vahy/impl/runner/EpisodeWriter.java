package vahy.impl.runner;

import vahy.api.episode.EpisodeResults;
import vahy.api.experiment.CommonAlgorithmConfig;
import vahy.api.experiment.Config;
import vahy.api.experiment.ProblemConfig;
import vahy.api.experiment.SystemConfig;
import vahy.api.model.Action;
import vahy.api.model.State;
import vahy.api.model.observation.Observation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EpisodeWriter<
    TAction extends Enum<TAction> & Action,
    TObservation extends Observation<TObservation>,
    TState extends State<TAction, TObservation, TState>> {

    private final Path rootPath;
    private final Path fullPath;

    public EpisodeWriter(ProblemConfig problemConfig, CommonAlgorithmConfig commonAlgorithmConfig, SystemConfig systemConfig, String timestamp, String policyName) {
        this.rootPath = systemConfig.getDumpPath();

        if(rootPath == null) {
            throw new IllegalArgumentException("Path for dumping data was not configured.");
        }

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
        printConfig(commonAlgorithmConfig, "CommonAlgorithmConfig", resultToFullPath);
        printConfig(systemConfig, "SystemConfig", resultToFullPath);
    }

    private void printConfig(Config config, String configName, File resultSubfolder) {
        File experimentSetupFile = new File(resultSubfolder, configName);
        try {
            var out = new PrintWriter(experimentSetupFile, Charset.defaultCharset());
            out.print(config.toFile());
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkFolderCreated(File resultFolder, boolean created) {
        if(!created) {
            throw new RuntimeException("Unable to create results folder: [" + resultFolder.getAbsolutePath() + "]");
        }
    }

    public void writeTrainingEpisode(int stageId, List<EpisodeResults<TAction, TObservation, TState>> episodeResults)  {
        for (int i = 0; i < episodeResults.size(); i++) {
            var formatted = String.format("%0" + String.valueOf(episodeResults.size()).length() + "d" , i);
            Path path = Paths.get(fullPath.toString(), "training", "stageId_" + stageId, "episodeId_" + formatted);
            createDirAndWriteData(episodeResults.get(i), path);
        }
    }

    public void writeEvaluationEpisode(List<EpisodeResults<TAction, TObservation, TState>> episodeResultsList) {
        for (int i = 0; i < episodeResultsList.size(); i++) {
            var formatted = String.format("%0" + String.valueOf(episodeResultsList.size()).length() + "d" , i);
            Path path = Paths.get(fullPath.toString(),"evaluation", "episodeId_" + formatted);
            createDirAndWriteData(episodeResultsList.get(i), path);
        }
    }

    private void createDirAndWriteData(EpisodeResults<TAction, TObservation, TState> episodeResults, Path path) {
        File file = new File(path.toUri());
        if(!file.exists()) {
            checkFolderCreated(file, file.mkdirs());
        }
        var filename = file.getAbsolutePath();
        try {
            writeEpisodeMetadata(filename, episodeResults);
            try(BufferedWriter outputWriter = new BufferedWriter(new FileWriter(Paths.get(filename, "data").toFile(), Charset.defaultCharset()))) {
                outputWriter.write(String.join(",", episodeResults.getEpisodeHistory().get(0).getCsvHeader()) + System.lineSeparator());
                for (int i = 0; i < episodeResults.getEpisodeHistory().size(); i++) {
                    outputWriter.write(String.join(",", episodeResults.getEpisodeHistory().get(i).getCsvRecord()) + System.lineSeparator());
                }
                outputWriter.flush();
            }

            try(BufferedWriter outputWriterStates = new BufferedWriter(new FileWriter(filename + "/stateDump", Charset.defaultCharset()))) {
                outputWriterStates.write(String.join(",", episodeResults.getEpisodeHistory().get(0).getFromState().getCsvHeader()) + System.lineSeparator());
                for (int i = 0; i < episodeResults.getEpisodeHistory().size(); i++) {
                    outputWriterStates.write(String.join(",", episodeResults.getEpisodeHistory().get(i).getFromState().getCsvRecord()) + System.lineSeparator());
                }
                outputWriterStates.write(String.join(",", episodeResults.getEpisodeHistory().get(episodeResults.getEpisodeHistory().size() - 1).getToState().getCsvRecord()) + System.lineSeparator());
                outputWriterStates.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeEpisodeMetadata(String filename, EpisodeResults<TAction, TObservation, TState> episodeResults) throws IOException {
        try(BufferedWriter outputWriter = new BufferedWriter(new FileWriter(filename + "/metadata", Charset.defaultCharset()))) {
            outputWriter.write(episodeResults.episodeMetadataToFile());
            outputWriter.flush();
        }
    }

}
