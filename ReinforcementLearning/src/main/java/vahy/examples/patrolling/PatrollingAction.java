package vahy.examples.patrolling;

import vahy.api.model.Action;

public enum PatrollingAction implements Action {

    ATTACK_000(0, 52, true),
    ATTACK_001(1, 52, true),
    ATTACK_002(2, 52, true),
    ATTACK_003(3, 52, true),
    ATTACK_004(4, 52, true),
    ATTACK_005(5, 52, true),
    ATTACK_006(6, 52, true),
    ATTACK_007(7, 52, true),
    ATTACK_008(8, 52, true),
    ATTACK_009(9, 52, true),
    ATTACK_010(10, 52, true),
    ATTACK_011(11, 52, true),
    ATTACK_012(12, 52, true),
    ATTACK_013(13, 52, true),
    ATTACK_014(14, 52, true),
    ATTACK_015(15, 52, true),
    ATTACK_016(16, 52, true),
    ATTACK_017(17, 52, true),
    ATTACK_018(18, 52, true),
    ATTACK_019(19, 52, true),
    ATTACK_020(20, 52, true),
    ATTACK_021(21, 52, true),
    ATTACK_022(22, 52, true),
    ATTACK_023(23, 52, true),
    ATTACK_024(24, 52, true),
    ATTACK_025(25, 52, true),
    ATTACK_026(26, 52, true),
    ATTACK_027(27, 52, true),
    ATTACK_028(28, 52, true),
    ATTACK_029(29, 52, true),
    ATTACK_030(30, 52, true),
    ATTACK_031(31, 52, true),
    ATTACK_032(32, 52, true),
    ATTACK_033(33, 52, true),
    ATTACK_034(34, 52, true),
    ATTACK_035(35, 52, true),
    ATTACK_036(36, 52, true),
    ATTACK_037(37, 52, true),
    ATTACK_038(38, 52, true),
    ATTACK_039(39, 52, true),
    ATTACK_040(40, 52, true),
    ATTACK_041(41, 52, true),
    ATTACK_042(42, 52, true),
    ATTACK_043(43, 52, true),
    ATTACK_044(44, 52, true),
    ATTACK_045(45, 52, true),
    ATTACK_046(46, 52, true),
    ATTACK_047(47, 52, true),
    ATTACK_048(48, 52, true),
    ATTACK_049(49, 52, true),

    WAIT(50, 52, true),
    SHADOW(51, 52, false),

    GO_TO_000(0, 50, false),
    GO_TO_001(1, 50, false),
    GO_TO_002(2, 50, false),
    GO_TO_003(3, 50, false),
    GO_TO_004(4, 50, false),
    GO_TO_005(5, 50, false),
    GO_TO_006(6, 50, false),
    GO_TO_007(7, 50, false),
    GO_TO_008(8, 50, false),
    GO_TO_009(9, 50, false),
    GO_TO_010(10, 50, false),
    GO_TO_011(11, 50, false),
    GO_TO_012(12, 50, false),
    GO_TO_013(13, 50, false),
    GO_TO_014(14, 50, false),
    GO_TO_015(15, 50, false),
    GO_TO_016(16, 50, false),
    GO_TO_017(17, 50, false),
    GO_TO_018(18, 50, false),
    GO_TO_019(19, 50, false),
    GO_TO_020(20, 50, false),
    GO_TO_021(21, 50, false),
    GO_TO_022(22, 50, false),
    GO_TO_023(23, 50, false),
    GO_TO_024(24, 50, false),
    GO_TO_025(25, 50, false),
    GO_TO_026(26, 50, false),
    GO_TO_027(27, 50, false),
    GO_TO_028(28, 50, false),
    GO_TO_029(29, 50, false),
    GO_TO_030(30, 50, false),
    GO_TO_031(31, 50, false),
    GO_TO_032(32, 50, false),
    GO_TO_033(33, 50, false),
    GO_TO_034(34, 50, false),
    GO_TO_035(35, 50, false),
    GO_TO_036(36, 50, false),
    GO_TO_037(37, 50, false),
    GO_TO_038(38, 50, false),
    GO_TO_039(39, 50, false),
    GO_TO_040(40, 50, false),
    GO_TO_041(41, 50, false),
    GO_TO_042(42, 50, false),
    GO_TO_043(43, 50, false),
    GO_TO_044(44, 50, false),
    GO_TO_045(45, 50, false),
    GO_TO_046(46, 50, false),
    GO_TO_047(47, 50, false),
    GO_TO_048(48, 50, false),
    GO_TO_049(49, 50, false);

    private final int localIndex;
    private final int sameEntityActionCount;
    private final boolean isAttackerTrueAction;

    PatrollingAction(int localIndex, int sameEntityActionCount, boolean isAttackerTrueAction) {
        this.localIndex = localIndex;
        this.sameEntityActionCount = sameEntityActionCount;
        this.isAttackerTrueAction = isAttackerTrueAction;
    }

    public boolean isAttackerTrueAction() {
        return isAttackerTrueAction;
    }

    @Override
    public int getLocalIndex() {
        return localIndex;
    }

    @Override
    public int getCountOfAllActionsFromSameEntity() {
        return sameEntityActionCount;
    }

    @Override
    public boolean isShadowAction() {
        return this.equals(SHADOW);
    }
}
