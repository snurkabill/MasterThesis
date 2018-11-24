package vahy.impl.search.node.nodeMetadata;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.impl.model.reward.DoubleScalarReward;
import vahy.impl.search.AbstractStateSpaceSearchTest;

public class BaseSearchNodeMetadataTest extends AbstractStateSpaceSearchTest {

    @Test
    public void simpleBaseSearchNodeMetadataTest() {
        DoubleScalarReward cumulativeReward = new DoubleScalarReward(42.0);
        DoubleScalarReward gainedReward = new DoubleScalarReward(43.0);
        DoubleScalarReward defaultTotalReward = new DoubleScalarReward(5.0);
        BaseSearchNodeMetadata<DoubleScalarReward> metadata = new BaseSearchNodeMetadata<>(cumulativeReward, gainedReward, defaultTotalReward);

        Assert.assertEquals(cumulativeReward.getValue(), metadata.getCumulativeReward().getValue(), DOUBLE_TOLERANCE);
        Assert.assertEquals(defaultTotalReward.getValue(), metadata.getDefaultEstimatedReward().getValue(), DOUBLE_TOLERANCE);
        Assert.assertEquals(defaultTotalReward.getValue(), metadata.getExpectedReward().getValue(), DOUBLE_TOLERANCE);
        Assert.assertEquals(gainedReward.getValue(), metadata.getGainedReward().getValue(), DOUBLE_TOLERANCE);
    }

}
