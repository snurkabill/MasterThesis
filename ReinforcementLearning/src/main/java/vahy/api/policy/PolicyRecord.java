package vahy.api.policy;

import java.util.List;

public interface PolicyRecord {

    double getPredictedReward();

    double[] getPolicyProbabilities();

    List<String> getCsvHeader();

    List<String> getCsvRecord();

    String toLogString();

}
