// 路径: common/src/main/java/com/videoshare/common/enums/VideoStatusEnum.java
package com.videoshare.common.enums;

public enum VideoStatusEnum {

    PENDING(0, "待审核"),
    PUBLISHED(1, "已发布"),
    OFFLINE(2, "已下架");

    private final Integer value;
    private final String displayName;

    VideoStatusEnum(Integer value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public Integer getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 从整数值解析枚举，无效值返回 null
     */
    public static VideoStatusEnum fromValue(Integer value) {
        if (value == null) return null;
        for (VideoStatusEnum e : values()) {
            if (e.value.equals(value)) return e;
        }
        return null;
    }

    /**
     * 校验从当前状态是否能转换到目标状态
     *
     * 允许的转换：
     *   PENDING  → PUBLISHED（管理员通过）
     *   PENDING  → OFFLINE  （管理员驳回）
     *   PUBLISHED → OFFLINE （用户/管理员下架）
     *   OFFLINE  → PUBLISHED（管理员上架/用户重新发布）
     */
    public boolean canTransitionTo(VideoStatusEnum target) {
        if (target == null || this == target) return false;
        switch (this) {
            case PENDING:
                return target == PUBLISHED || target == OFFLINE;
            case PUBLISHED:
                return target == OFFLINE;
            case OFFLINE:
                return target == PUBLISHED;
            default:
                return false;
        }
    }
}
