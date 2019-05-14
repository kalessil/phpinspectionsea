package com.kalessil.phpStorm.phpInspectionsEA.openApi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

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

    @Nullable
    static private PhpLanguageLevel current;

    PhpLanguageLevel(@NotNull String version) {
        this.version = version;
    }

    @NotNull
    public String getVersion() {
        return this.version;
    }

    static public void force(@Nullable PhpLanguageLevel level) {
        current = level;
    }

    static public PhpLanguageLevel fromString(@NotNull String version) {
        if (current == null) {
            return Arrays.stream(values())
                    .filter(level -> level.getVersion().equals(version))
                    .findFirst()
                    .orElse(PHP740);
        }
        return current;
    }
}
