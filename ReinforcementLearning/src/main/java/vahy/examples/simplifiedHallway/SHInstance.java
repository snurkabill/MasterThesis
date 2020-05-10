package vahy.examples.simplifiedHallway;

public enum SHInstance {

    DEMO_01("examples/hallway_demo0.txt"),

    GOLD_EVERYWHERE("examples/goldEverywhere.txt"),

    BENCHMARK_00("examples/benchmark/benchmark_00.txt"),
    BENCHMARK_01("examples/benchmark/benchmark_01.txt"),
    BENCHMARK_02("examples/benchmark/benchmark_02.txt"),
    BENCHMARK_03("examples/benchmark/benchmark_03.txt"),
    BENCHMARK_04("examples/benchmark/benchmark_04.txt"),
    BENCHMARK_05("examples/benchmark/benchmark_05.txt"),
    BENCHMARK_06("examples/benchmark/benchmark_06.txt"),
    BENCHMARK_07("examples/benchmark/benchmark_07.txt"),
    BENCHMARK_08("examples/benchmark/benchmark_08.txt"),
    BENCHMARK_09("examples/benchmark/benchmark_09.txt"),

    BENCHMARK_10("examples/benchmark/benchmark_10.txt"),
    BENCHMARK_11("examples/benchmark/benchmark_11.txt"),

    BENCHMARK_12("examples/benchmark/benchmark_12.txt"),
    BENCHMARK_13("examples/benchmark/benchmark_13.txt"),
    BENCHMARK_14("examples/benchmark/benchmark_14.txt"),
    BENCHMARK_15("examples/benchmark/benchmark_15.txt"),
    BENCHMARK_16("examples/benchmark/benchmark_16.txt"),
    BENCHMARK_17("examples/benchmark/benchmark_17.txt"),
    BENCHMARK_18("examples/benchmark/benchmark_18.txt"),
    BENCHMARK_19("examples/benchmark/benchmark_19.txt");

    private String path;

    SHInstance(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

}
