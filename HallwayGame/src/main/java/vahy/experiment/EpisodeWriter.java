package vahy.experiment;

import vahy.api.episode.EpisodeResults;
import vahy.config.PaperAlgorithmConfig;
import vahy.environment.HallwayAction;
import vahy.environment.config.GameConfig;
import vahy.environment.state.EnvironmentProbabilities;
import vahy.environment.state.HallwayStateImpl;
import vahy.impl.model.observation.DoubleVector;
import vahy.paperGenerics.policy.PaperPolicyRecord;
import vahy.utils.ImmutableTuple;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EpisodeWriter {

    private final String masterPath;

    public EpisodeWriter(GameConfig gameConfig, PaperAlgorithmConfig algorithmConfig) {
        String resultMasterFolderName = "Results";
        File resultFolder = new File(resultMasterFolderName);

        if(!resultFolder.exists()) {
            resultFolder.mkdir();
        }

        File resultSubfolder = new File(resultFolder, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss")));
        resultSubfolder.mkdir();

        masterPath = resultSubfolder.getAbsolutePath();

        File experimentSetupFile = new File(resultSubfolder, "algorithmConfig");
        PrintWriter out = null;
        try {
            out = new PrintWriter(experimentSetupFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        out.print("AlgorithmConfig: " + System.lineSeparator() + algorithmConfig.toString() + System.lineSeparator() + System.lineSeparator() + "GameSetup: " + System.lineSeparator() + gameConfig.toString());
        out.close();
    }

    public void writeTrainingEpisode(int stageId, List<EpisodeResults<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl, PaperPolicyRecord>> episodeResults)  {
        for (int i = 0; i < episodeResults.size(); i++) {
            String path = masterPath + "/training/stageId_" + stageId + "/episodeId_" + i;
            createDirAndWriteData(episodeResults.get(i), path);
        }


    }

    public void writeEvaluationEpisode(List<EpisodeResults<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl, PaperPolicyRecord>> episodeResultsList) {
        for (int i = 0; i < episodeResultsList.size(); i++) {
            String path = masterPath + "/evaluation/" + "/episodeId_" + i;
            createDirAndWriteData(episodeResultsList.get(i), path);
        }
    }

    private void createDirAndWriteData(EpisodeResults<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl, PaperPolicyRecord> episodeResults, String path) {
        File file = new File(path);
        if(!file.exists()) {
            var created = file.mkdirs();
            if(!created) {
                throw new RuntimeException("directory was not created");
            }
        }
        dumpSingleEpisode(file.getAbsolutePath(), episodeResults);
    }

    public static void dumpSingleEpisode(String filename, EpisodeResults<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl, PaperPolicyRecord> episodeResults) {
        try {
            writeEpisodeMetadata(filename, episodeResults);
            BufferedWriter outputWriter = new BufferedWriter(new FileWriter(filename + "/data"));
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

    private static void writeEpisodeMetadata(String filename, EpisodeResults<HallwayAction, DoubleVector, EnvironmentProbabilities, HallwayStateImpl, PaperPolicyRecord> episodeResults) throws IOException {
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(filename + "/metadata"));

        outputWriter.write("Total step count, " + episodeResults.getTotalStepCount() + System.lineSeparator());
        outputWriter.write("Player step count, " + episodeResults.getPlayerStepCount() + System.lineSeparator());
        outputWriter.write("Duration [ms], " + episodeResults.getDuration().toMillis() + System.lineSeparator());
        outputWriter.write("Total Payoff, " + episodeResults.getTotalPayoff() + System.lineSeparator());
        outputWriter.write("Risk Hit, " + episodeResults.getFinalState().isRiskHit() + System.lineSeparator());


        outputWriter.flush();
        outputWriter.close();
    }

    public static void writeEpisodeResultsToFile(String filename, List<ImmutableTuple<Double, Boolean>> list) throws IOException {
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(filename));
        outputWriter.write("Reward,Risk");
        outputWriter.newLine();
        for (int i = 0; i < list.size(); i++) {
            outputWriter.write(list.get(i).getFirst() + "," + list.get(i).getSecond());
            outputWriter.newLine();
        }
        outputWriter.flush();
        outputWriter.close();
    }
}
