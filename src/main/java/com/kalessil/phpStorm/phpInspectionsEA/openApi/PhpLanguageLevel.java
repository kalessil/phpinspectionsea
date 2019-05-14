package com.kalessil.phpStorm.phpInspectionsEA.openApi;

import org.jetbrains.annotations.NotNull;

public enum PhpLanguageLevel {
    PHP530("5.3.0"),
    PHP540("5.4.0"),
    PHP550("5.5.0"),
    PHP560("5.6.0"),
    PHP700("7"),
    PHP710("7.1"),
    PHP720("7.2"),
    PHP730("7.3"),
    PHP740("7.4");

    @NotNull
    private final String version;

    PhpLanguageLevel(@NotNull String version) {
        this.version = version;
    }

    @NotNull
    public String getVersion() {
        return this.version;
    }

    static public PhpLanguageLevel fromString(@NotNull String version) {
        for (final PhpLanguageLevel level : values()) {
            if (level.getVersion().equals(version)) {
                return level;
            }
        }
        return PHP740;
    }
}
