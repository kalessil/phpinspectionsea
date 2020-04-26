package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit;

import org.jetbrains.annotations.NotNull;

public enum PhpUnitVersion {
    /* Note from March 2020: versions according current PhpUnit documentation versiosn */
    PHPUNIT70("7.0"),
    PHPUNIT71("7.1"),
    PHPUNIT72("7.2"),
    PHPUNIT73("7.3"),
    PHPUNIT74("7.4"),
    PHPUNIT75("7.5"),
    PHPUNIT80("8.0"),
    PHPUNIT81("8.1"),
    PHPUNIT82("8.2"),
    PHPUNIT83("8.3"),
    PHPUNIT84("8.4"),
    PHPUNIT85("8.5"),
    PHPUNIT90("9.0"),
    PHPUNIT91("9.1");

    @NotNull
    private final String version;

    PhpUnitVersion(@NotNull String version) {
        this.version = version;
    }

    public boolean atLeast(@NotNull PhpUnitVersion version) {
        return this.compareTo(version) >= 0;
    }

    @Override
    public String toString() {
        return this.version;
    }
}
