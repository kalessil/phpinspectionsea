package com.kalessil.phpStorm.phpInspectionsEA.settings;

import com.kalessil.phpStorm.phpInspectionsEA.EASettings;
import org.jetbrains.annotations.NotNull;

public enum ComparisonStyle {
    REGULAR("Regular"),
    YODA("Yoda");

    private final String value;

    ComparisonStyle(@NotNull String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static boolean isRegular() {
        return EASettings.getInstance().getComparisonStyle() != YODA;
    }

    public static void force(@NotNull ComparisonStyle style) {
        EASettings.getInstance().setComparisonStyle(style);
    }
}
