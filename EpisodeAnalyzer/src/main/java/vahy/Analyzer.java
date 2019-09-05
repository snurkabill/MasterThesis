package vahy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.utils.ImmutableTuple;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Analyzer {

    private static final Logger logger = LoggerFactory.getLogger(Analyzer.class.getName());

    public static void main(String args[]) throws IOException {


        processAllData("c:\\Users\\Snurka\\Documents\\ralf_vysledky\\Results\\");

//        analyzeEpisodes("c:\\Users\\Snurka\\devel\\repositories\\Thesis\\MasterThesis\\Results\\2019_09_04_14_01_07\\Rewards");
    }


    public static void processAllData(String pathToMasterFolder) throws IOException {

        File file = new File(pathToMasterFolder);
        File[] directories = file.listFiles(File::isDirectory);

        for (File directory : directories) {

            logger.info(System.lineSeparator());
            logger.info(System.lineSeparator());
            logger.info(System.lineSeparator());
            logger.info("Analyzing file: [{}]", directory.getName());

            analyzeEpisodes(new File(directory.getAbsolutePath(), "Rewards").getAbsolutePath());
        }
    }



    public static void analyzeEpisodes(String fileName) throws IOException {
        Stream<String> stream = Files.lines(Paths.get(fileName)).skip(1);

        var results = stream.map(x -> {
            String[] parts = x.split(",");
            return new ImmutableTuple<>(Double.valueOf(parts[0]), Boolean.valueOf(parts[1]));
        }).collect(Collectors.toList());

        printStatistics(results, x -> true, "All episodes");
        printStatistics(results, ImmutableTuple::getSecond, "All unsuccessful episodes");
        printStatistics(results, x -> !x.getSecond(), "All successful episodes");

    }

    public static double calculateAverage(List<Double> data) {
        return data.stream().mapToDouble(x -> x).sum() / data.size();
    }

    public static double calculateStdev(List<Double> data) {
        var average = calculateAverage(data);
        var innerSum = data.stream()
            .mapToDouble(x -> {
                var diff = x - average;
                return diff * diff;
            }).sum();
        return Math.sqrt(innerSum * (1.0 / (data.size() - 1)));
    }

    public static void printStatistics(List<ImmutableTuple<Double, Boolean>> data, Predicate<ImmutableTuple<Double, Boolean>> filter, String dataName) {
        var filteredData = data.stream().filter(filter).map(ImmutableTuple::getFirst).collect(Collectors.toList());

        var average = calculateAverage(filteredData);
        var stdev = calculateStdev(filteredData);
        logger.info("Dataset Name: [{}]", dataName);
        logger.info("Count: [{}]", filteredData.size());
        logger.info("Average: [{}]", average);
        if(filteredData.size() < 2) {
            logger.info("Dataset name: [{}] has only [{}] values. Can't calculate stdev.", dataName, filteredData.size());
        } else {
            logger.info("Stdev: [{}]", stdev);
        }
    }


}
