package vahy.data;

public enum HallwayInstance {

    DEMO_01("examples/hallway_demo0.txt", 1),

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

    BENCHMARK_12("examples/benchmark/benchmark_12.txt", 12),
    BENCHMARK_13("examples/benchmark/benchmark_13.txt", 18),
    BENCHMARK_14("examples/benchmark/benchmark_14.txt", 11),
    BENCHMARK_15("examples/benchmark/benchmark_15.txt", 19),
    BENCHMARK_16("examples/benchmark/benchmark_16.txt", 27),
    BENCHMARK_17("examples/benchmark/benchmark_17.txt", 54),
    BENCHMARK_18("examples/benchmark/benchmark_18.txt", 10),
    BENCHMARK_19("examples/benchmark/benchmark_19.txt", 10);



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
