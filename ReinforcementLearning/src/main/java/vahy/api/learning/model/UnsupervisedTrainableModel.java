package vahy.api.learning.model;

public interface UnsupervisedTrainableModel extends Model {

    void fit(double[][] inputs);
}
