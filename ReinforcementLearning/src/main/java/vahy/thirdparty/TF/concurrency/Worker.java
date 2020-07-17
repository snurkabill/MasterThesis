package vahy.thirdparty.TF.concurrency;

import vahy.impl.predictor.tf.TFModelImproved;

import java.util.concurrent.Callable;

public class Worker implements Callable<double[][]> {

    private final TFModelImproved wrapper;
    private final double[][][] batches;
    private final double[][][] predictions;

    public Worker(TFModelImproved wrapper, int startFromIndex, int endAtIndex, double[][] sourceData, int batchSize) {
        if((endAtIndex - startFromIndex) % batchSize != 0) {
            throw new IllegalStateException("Can't divide by batch size");
        }
        int batchCount = (endAtIndex - startFromIndex) / batchSize;
        batches = new double[batchCount][][];
        int counter = startFromIndex;
        for (int i = 0; i < batchCount; i++) {
            batches[i] = new double[batchSize][];
            for (int j = 0; j < batchSize; j++) {
                batches[i][j] = new double[sourceData[0].length];
                for (int k = 0; k < sourceData[0].length; k++) {
                    batches[i][j][k] = sourceData[counter][k];
                }
                counter++;
            }
        }
        this.wrapper = wrapper;
        this.predictions = new double[batchCount][][];
    }

    @Override
    public double[][] call() throws Exception {
        for (int i = 0; i < batches.length; i++) {
            predictions[i] = wrapper.predict(batches[i]);
        }

        var final_predictions = new double[predictions.length * predictions[0].length][];
        int counter = 0;
        for (int i = 0; i < predictions.length; i++) {
            for (int j = 0; j < predictions[0].length; j++) {
                final_predictions[counter] = predictions[i][j];
                counter++;
            }
        }
        return final_predictions;
    }
}

























