package vahy.api.learning.model;

public interface SupervisedTrainableModel extends Model {

    void fit(double[][] input, double[][] target);
}
