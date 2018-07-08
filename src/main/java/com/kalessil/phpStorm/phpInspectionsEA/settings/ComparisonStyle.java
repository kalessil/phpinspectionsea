package com.kalessil.phpStorm.phpInspectionsEA.settings;

import com.kalessil.phpStorm.phpInspectionsEA.EASettings;
import org.jetbrains.annotations.Nullable;

public enum ComparisonStyle {
    REGULAR("Regular"),
    YODA("Yoda");

    private final String value;

    @Nullable private static ComparisonStyle temporary = null;

    ComparisonStyle(final String settingValue) {
        value = settingValue;
    }

    public String getValue() {
        return value;
    }

    public static Boolean isRegular() {
        if (temporary != null) {
            return temporary == REGULAR;
        }

        return EASettings.getInstance().getComparisonStyle() != YODA;
    }

    public static void setTemporarily(@Nullable final ComparisonStyle comparisonStyle) {
        temporary = comparisonStyle;
    }
}
