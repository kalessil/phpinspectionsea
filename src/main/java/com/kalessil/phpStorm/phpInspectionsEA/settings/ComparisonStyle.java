package com.kalessil.phpStorm.phpInspectionsEA.settings;

public enum ComparisonStyle {
    REGULAR("Regular"),
    YODA("Yoda");

    private final String value;

    ComparisonStyle(final String settingValue) {
        value = settingValue;
    }

    public String getValue() {
        return value;
    }
}
