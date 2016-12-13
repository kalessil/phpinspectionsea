package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.jetbrains.php.lang.psi.elements.PhpClass;
import org.jetbrains.annotations.NotNull;

final public class FileSystemUtil {
    public static boolean isTestClass(@NotNull PhpClass clazz) {
        /* first criteria - file name */
        final String fileName = clazz.getContainingFile().getName();
        if (fileName.endsWith("Test.php") || fileName.endsWith("Spec.php") || fileName.endsWith(".phpt")) {
            return true;
        }

        /* second criteria: FQN containing \Test[s]\ or ends with Test */
        final String classFqn = clazz.getFQN();
        return classFqn.endsWith("Test") || classFqn.contains("\\Tests\\") || classFqn.contains("\\Test\\");
    }
}
