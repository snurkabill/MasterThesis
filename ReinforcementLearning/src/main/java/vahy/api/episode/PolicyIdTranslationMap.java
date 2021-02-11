package vahy.api.episode;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class PolicyIdTranslationMap {

    public static final int INITIAL_POLICY_POOL_SIZE = 10;

    private final BiMap<Integer, Integer> map;

    public PolicyIdTranslationMap() {
        this(INITIAL_POLICY_POOL_SIZE);
    }

    public PolicyIdTranslationMap(int expectedInitialSize) {
        this.map = HashBiMap.create(expectedInitialSize);
    }

    public void put(int policyId, int inGameEntityId) {
        map.put(policyId, inGameEntityId);
    }

    public Integer getInGameEntityId(int policyId) {
        return map.get(policyId);
    }

    public int getPolicyId(int inGameEntityId) {
        return map.inverse().get(inGameEntityId);
    }
}
