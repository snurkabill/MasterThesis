package vahy.utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ThirdPartBinaryUtils {

    public static void cleanUpNativeTempFiles() {
        System.gc();
        String bridJFolderNameStart = "BridJExtractedLibraries";
        String CLPFolderNameStart = "CLPExtractedLib";
        String TFFolderNameStart = "tensorflow_native_libraries";
        String tempPath = System.getProperty("java.io.tmpdir");
        File file = new File(tempPath);
        String[] directories = file.list((current, name) -> new File(current, name).isDirectory());
        if (directories != null) {
            Arrays.stream(directories).filter(x -> x.startsWith(bridJFolderNameStart) || x.startsWith(CLPFolderNameStart) || x.startsWith(TFFolderNameStart)).forEach(x -> {
                try {
                    FileUtils.deleteDirectory(new File(tempPath + "/" + x));
                } catch (IOException e) {
                    e.printStackTrace(); // todo: deal with this later
                }
            });
        }
    }
}
