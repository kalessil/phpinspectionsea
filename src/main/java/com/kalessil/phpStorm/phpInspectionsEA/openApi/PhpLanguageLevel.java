package com.kalessil.phpStorm.phpInspectionsEA.openApi;

import com.intellij.openapi.project.Project;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public enum PhpLanguageLevel {
    PHP530("5.3.0"),
    PHP540("5.4.0"),
    PHP550("5.5.0"),
    PHP560("5.6.0"),
    PHP700("7.0"),
    PHP710("7.1"),
    PHP720("7.2"),
    PHP730("7.3"),
    PHP740("7.4"),
    PHP800("8.0"),
    PHP810("8.1");

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

    public boolean atLeast(@NotNull PhpLanguageLevel version) {
        return this.compareTo(version) >= 0;
    }

    public boolean below(@NotNull PhpLanguageLevel version) {
        return this.compareTo(version) < 0;
    }

    static public void set(@Nullable PhpLanguageLevel level) {
        current = level;
    }

    static public PhpLanguageLevel get(@NotNull Project project) {
        if (current == null) {
            final String version = PhpProjectConfigurationFacade.getInstance(project).getLanguageLevel().getVersionString();
            return Arrays.stream(values())
                    .filter(level -> level.getVersion().equals(version.equals("7") ? "7.0" : version))
                    .findFirst()
                    .orElse(PHP740);
        }
        return current;
    }
}
