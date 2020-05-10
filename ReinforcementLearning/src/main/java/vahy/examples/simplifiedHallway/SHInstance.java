package vahy.examples.simplifiedHallway;

public enum SHInstance {

    DEMO_01("examples/simplifiedHalllway/benchmark/hallway_demo0.txt"),

    GOLD_EVERYWHERE("examples/simplifiedHalllway/benchmark/goldEverywhere.txt"),

    BENCHMARK_00("examples/simplifiedHalllway/benchmark/benchmark_00.txt"),
    BENCHMARK_01("examples/simplifiedHalllway/benchmark/benchmark_01.txt"),
    BENCHMARK_02("examples/simplifiedHalllway/benchmark/benchmark_02.txt"),
    BENCHMARK_03("examples/simplifiedHalllway/benchmark/benchmark_03.txt"),
    BENCHMARK_04("examples/simplifiedHalllway/benchmark/benchmark_04.txt"),
    BENCHMARK_05("examples/simplifiedHalllway/benchmark/benchmark_05.txt"),
    BENCHMARK_06("examples/simplifiedHalllway/benchmark/benchmark_06.txt"),
    BENCHMARK_07("examples/simplifiedHalllway/benchmark/benchmark_07.txt"),
    BENCHMARK_08("examples/simplifiedHalllway/benchmark/benchmark_08.txt"),
    BENCHMARK_09("examples/simplifiedHalllway/benchmark/benchmark_09.txt"),

    BENCHMARK_10("examples/simplifiedHalllway/benchmark/benchmark_10.txt"),
    BENCHMARK_11("examples/simplifiedHalllway/benchmark/benchmark_11.txt"),

    BENCHMARK_12("examples/simplifiedHalllway/benchmark/benchmark_12.txt"),
    BENCHMARK_13("examples/simplifiedHalllway/benchmark/benchmark_13.txt"),
    BENCHMARK_14("examples/simplifiedHalllway/benchmark/benchmark_14.txt"),
    BENCHMARK_15("examples/simplifiedHalllway/benchmark/benchmark_15.txt"),
    BENCHMARK_16("examples/simplifiedHalllway/benchmark/benchmark_16.txt"),
    BENCHMARK_17("examples/simplifiedHalllway/benchmark/benchmark_17.txt"),
    BENCHMARK_18("examples/simplifiedHalllway/benchmark/benchmark_18.txt"),
    BENCHMARK_19("examples/simplifiedHalllway/benchmark/benchmark_19.txt");

    private String path;

    SHInstance(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

}
