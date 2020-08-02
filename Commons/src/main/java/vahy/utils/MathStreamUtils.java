package vahy.utils;

import java.util.List;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

public class MathStreamUtils {

    private MathStreamUtils() {
    }

    public static double calculateAverage(List<Double> data) {
        return calculateAverage(data.stream(), value -> value);
    }

    public static <T> double calculateAverage(List<T> data, ToDoubleFunction<T> mapper) {
        return calculateAverage(data.stream(), mapper);
    }

    public static <T> double calculateAverage(Stream<T> data, ToDoubleFunction<T> mapper) {
        return data.mapToDouble(mapper).average().orElseThrow(() -> new IllegalStateException("Average does not exists"));
    }

    public static double calculateStdev(List<Double> data) {
        return calculateStdev(data, calculateAverage(data));
    }

    public static <T> double calculateStdev(List<T> data, ToDoubleFunction<T> mapper) {
        return calculateStdev(data, mapper, calculateAverage(data, mapper));
    }

    public static double calculateStdev(List<Double> data, double average) {
        return calculateStdev(data, x -> x, average);
    }

    public static <T> double calculateStdev(List<T> data, ToDoubleFunction<T> mapper, double average) {
        var innerSum = data.stream().mapToDouble(mapper)
            .map(x -> {
                var diff = x - average;
                return diff * diff;
            }).sum();
        return Math.sqrt(innerSum * (1.0 / (data.size() - 1)));
    }
}
