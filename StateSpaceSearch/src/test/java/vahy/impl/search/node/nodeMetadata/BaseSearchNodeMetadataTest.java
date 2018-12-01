package vahy.impl.search.node.nodeMetadata;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.impl.model.reward.DoubleReward;
import vahy.impl.search.AbstractStateSpaceSearchTest;

public class BaseSearchNodeMetadataTest extends AbstractStateSpaceSearchTest {

    @Test
    public void simpleBaseSearchNodeMetadataTest() {
        DoubleReward cumulativeReward = new DoubleReward(42.0);
        DoubleReward gainedReward = new DoubleReward(43.0);
        DoubleReward defaultTotalReward = new DoubleReward(5.0);
        BaseSearchNodeMetadata<DoubleReward> metadata = new BaseSearchNodeMetadata<>(cumulativeReward, gainedReward, defaultTotalReward);

        Assert.assertEquals(cumulativeReward.getValue(), metadata.getCumulativeReward().getValue(), DOUBLE_TOLERANCE);
        Assert.assertEquals(defaultTotalReward.getValue(), metadata.getPredictedReward().getValue(), DOUBLE_TOLERANCE);
        Assert.assertEquals(defaultTotalReward.getValue(), metadata.getExpectedReward().getValue(), DOUBLE_TOLERANCE);
        Assert.assertEquals(gainedReward.getValue(), metadata.getGainedReward().getValue(), DOUBLE_TOLERANCE);
    }

}
