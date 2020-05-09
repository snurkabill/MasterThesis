package vahy.impl.search;

import org.testng.Assert;
import org.testng.annotations.Test;
import vahy.impl.search.node.nodeMetadata.BaseSearchNodeMetadata;

public class BaseSearchNodeMetadataTest extends AbstractStateSpaceSearchTest {

    @Test
    public void simpleBaseSearchNodeMetadataTest() {
        double cumulativeReward = 42.0;
        double gainedReward = 43.0;
        double defaultTotalReward = 5.0;
        BaseSearchNodeMetadata metadata = new BaseSearchNodeMetadata(cumulativeReward, gainedReward, defaultTotalReward);

        Assert.assertEquals(cumulativeReward, metadata.getCumulativeReward(), DOUBLE_TOLERANCE);
        Assert.assertEquals(defaultTotalReward, metadata.getPredictedReward(), DOUBLE_TOLERANCE);
        Assert.assertEquals(defaultTotalReward, metadata.getExpectedReward(), DOUBLE_TOLERANCE);
        Assert.assertEquals(gainedReward, metadata.getGainedReward(), DOUBLE_TOLERANCE);
    }

}
