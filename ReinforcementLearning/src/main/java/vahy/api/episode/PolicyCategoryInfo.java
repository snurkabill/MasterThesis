package vahy.api.episode;

public class PolicyCategoryInfo {

    private final boolean isShufflePossible;
    private final int categoryId;
    private final int policyInCategoryCount;

    public PolicyCategoryInfo(boolean isShufflePossible, int categoryId, int policyInCategoryCount) {
        this.isShufflePossible = isShufflePossible;
        this.categoryId = categoryId;
        this.policyInCategoryCount = policyInCategoryCount;
    }

    public boolean isShufflePossible() {
        return isShufflePossible;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getPolicyInCategoryCount() {
        return policyInCategoryCount;
    }
}
