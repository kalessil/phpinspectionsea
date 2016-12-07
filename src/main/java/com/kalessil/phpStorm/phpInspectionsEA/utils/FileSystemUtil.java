package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

final public class FileSystemUtil {
    public static boolean isTestClass(@NotNull PhpClass clazz) {
        final String fqn = clazz.getFQN();
        return !StringUtil.isEmpty(fqn) && (fqn.endsWith("Test") || fqn.contains("\\Tests\\") || fqn.contains("\\Test\\"));
    }
}
