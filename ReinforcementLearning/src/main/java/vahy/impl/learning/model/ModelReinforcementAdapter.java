package vahy.impl.learning.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vahy.api.learning.model.SupervisedTrainableModel;
import vahy.api.learning.model.SupervisedTrainableValueModel;
import vahy.api.model.reward.RewardFactory;
import vahy.api.model.reward.DoubleVectorialReward;
import vahy.impl.model.observation.DoubleVectorialObservation;

import java.util.List;

public class ModelReinforcementAdapter<TReward extends DoubleVectorialReward> implements SupervisedTrainableValueModel<TReward, DoubleVectorialObservation> {

    private static final Logger logger = LoggerFactory.getLogger(ModelReinforcementAdapter.class);

    private final RewardFactory<TReward> rewardFactory;
    private final SupervisedTrainableModel model;

    public ModelReinforcementAdapter(SupervisedTrainableModel model,
                                     RewardFactory<TReward> rewardFactory) {
        this.rewardFactory = rewardFactory;
        this.model = model;
        logger.debug("Created [{}] with function approximator: [{}]", ModelReinforcementAdapter.class.getName(), model.toString());
    }

    @Override
    public TReward approximateReward(DoubleVectorialObservation observationAggregation) {
        return rewardFactory.fromNumericVector(model.predict(observationAggregation.getObservedVector()));
    }

    @Override
    public void fit(List<DoubleVectorialObservation> inputList, List<TReward> rewardList) {
        if(inputList.size() != rewardList.size()) {
            throw new IllegalArgumentException(
                "Input matrix and reward list differs in common dimension. Input matrix dim: [" + inputList.size() + "], reward list dim: [" + rewardList.size() + "]");
        }
        double[][] input = new double[inputList.size()][];
        double[][] output = new double[rewardList.size()][];
        for (int i = 0; i < inputList.size(); i++) {
            input[i] = inputList.get(i).getObservedVector();
            output[i] = rewardList.get(i).getAsVector();
        }
        model.fit(input, output);
    }


//    private double[] getInput(List<TObservation> observationList) {
//        if(observationList.isEmpty()) {
//            throw new IllegalArgumentException("Observation list is empty");
//        }
//        int observationVectorLength = observationList.get(0).getObservedVector().length;
//        if(model.getInputDimension() != observationVectorLength * observationList.size()) {
//            throw new IllegalArgumentException("Expected input length: [" + model.getInputDimension() + "]. Actual length: [" + observationVectorLength + "]");
//        }
//        double[] input = new double[model.getInputDimension()];
//        for (int observationIndex = 0, k = 0; observationIndex < observationList.size(); observationIndex++) {
//            for (int i = 0; i < observationList.get(0).getObservedVector().length; i++, k++) {
//                input[k] = observationList.get(observationIndex).getObservedVector()[i];
//            }
//        }
//        return input;
//    }
}
