package vahy.vizualization;

public class LabelData {

    private final String name;
    private final Double value;

    public LabelData(String name, Double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Double getValue() {
        return value;
    }
}
