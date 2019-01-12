package vahy.data;

public enum HallwayInstance {

    BENCHMARK_01("examples/benchmark/benchmark_01.txt"),
    BENCHMARK_02("examples/benchmark/benchmark_02.txt"),
    BENCHMARK_03("examples/benchmark/benchmark_03.txt"),
    BENCHMARK_04("examples/benchmark/benchmark_04.txt"),
    BENCHMARK_05("examples/benchmark/benchmark_05.txt"),
    BENCHMARK_06("examples/benchmark/benchmark_06.txt"),
    BENCHMARK_07("examples/benchmark/benchmark_07.txt"),
    BENCHMARK_08("examples/benchmark/benchmark_08.txt"),
    BENCHMARK_09("examples/benchmark/benchmark_09.txt");

    private String path;

    HallwayInstance(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
