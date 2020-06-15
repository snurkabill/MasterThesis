package vahy.impl.predictor.tf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.experiment.ApproximatorConfig;
import vahy.api.experiment.SystemConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TFHelper {

    public static final Logger logger = LoggerFactory.getLogger(TFHelper.class.getName());

    public static byte[] loadTensorFlowModel(ApproximatorConfig approximatorConfig, SystemConfig systemConfig, int inputCount, int outputActionCount) throws IOException, InterruptedException {
        var modelName = "tfModel_" + LocalDateTime.now().atZone(ZoneOffset.UTC);
        modelName = modelName.replace(":", "_");
        Process process = Runtime.getRuntime().exec(systemConfig.getPythonVirtualEnvPath()
            + " " +
            Paths.get("PythonScripts", "tensorflow_models", approximatorConfig.getCreatingScript()) +
            " " +
            modelName +
            " " +
            inputCount +
            " " +
            outputActionCount +
            " " +
            Paths.get("PythonScripts", "generated_models") +
            " " +
            (int)systemConfig.getRandomSeed());

        try(BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader error = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String line;
            String line2;

            while ((line = input.readLine()) != null) {
                logger.info(line);
            }
            while ((line2 = error.readLine()) != null) {
                logger.error(line2);
            }
        }
        var exitValue = process.waitFor();
        if(exitValue != 0) {
            throw new IllegalStateException("Python process ended with non-zero exit value. Exit val: [" + exitValue + "]");
        }
        var dir = new File(Paths.get("PythonScripts", "generated_models").toString());
        Files.createDirectories(dir.toPath());
        return Files.readAllBytes(new File(dir, modelName + ".pb").toPath());
    }

}
