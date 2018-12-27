package vahy.impl.learning.model;

public class MutableDoubleArray {

    private final double[] doubleArray;
    private int counter;

    public MutableDoubleArray(double[] doubleArray, boolean isEmpty) {
        this.doubleArray = doubleArray;
        counter = isEmpty ? 0 : 1;
    }

    public void addDataSample(double[] newSample) {
        if(newSample.length != doubleArray.length) {
            throw new IllegalArgumentException("Lengths of arrays differ");
        }
        for (int i = 0; i < doubleArray.length; i++) {
            doubleArray[i] = ((doubleArray[i] * counter) + newSample[i]) / (counter + 1);
        }
        counter++;
    }

    public double[] getDoubleArray() {
        return doubleArray;
    }

    public int getCounter() {
        return counter;
    }
}
