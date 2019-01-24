package vahy.data;

public enum HallwayInstance {

    BENCHMARK_01("examples/benchmark/benchmark_01.txt", 8),
    BENCHMARK_02("examples/benchmark/benchmark_02.txt", 7),
    BENCHMARK_03("examples/benchmark/benchmark_03.txt", 7),
    BENCHMARK_04("examples/benchmark/benchmark_04.txt", 9),
    BENCHMARK_05("examples/benchmark/benchmark_05.txt", 9),
    BENCHMARK_06("examples/benchmark/benchmark_06.txt", 7),
    BENCHMARK_07("examples/benchmark/benchmark_07.txt", 7),
    BENCHMARK_08("examples/benchmark/benchmark_08.txt", 8),
    BENCHMARK_09("examples/benchmark/benchmark_09.txt", 10),

    BENCHMARK_10("examples/benchmark/benchmark_10.txt", 8),
    BENCHMARK_11("examples/benchmark/benchmark_11.txt", 10),

    BENCHMARK_12("examples/benchmark/benchmark_12.txt", 12);



    private String path;
    private int observationLengthInCompactMode;

    HallwayInstance(String path, int observationLengthInCompactMode) {
        this.path = path;
        this.observationLengthInCompactMode = observationLengthInCompactMode;
    }

    public String getPath() {
        return path;
    }

    public int getObservationLengthInCompactMode() {
        return observationLengthInCompactMode;
    }
}
