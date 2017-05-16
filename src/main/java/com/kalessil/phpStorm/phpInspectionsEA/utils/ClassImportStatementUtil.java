package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpUse;
import com.jetbrains.php.lang.psi.elements.PhpUseList;

final public class ClassImportStatementUtil {
    public ImportStatus canImportClass(PhpFile file, String className, String FQCN) {
        PsiElement importMarker = null;
        Boolean isImported = false;
        Boolean isCollision = false;
        String importName = FQCN;

        for (PhpUseList useList : PsiTreeUtil.findChildrenOfType(file, PhpUseList.class)) {
            final PsiElement useParent = useList.getParent();

            if (useParent instanceof Function || useParent instanceof PhpClass) {
                continue;
            }

            importMarker = useList;
            for (PsiElement use : useList.getChildren()) {
                if (!(use instanceof PhpUse)) {
                    continue;
                }

                final PhpUse useStatement = (PhpUse) use;

                if (useStatement.getFQN().equals(FQCN)) {
                    isImported = true;
                    importName = useStatement.getName();
                    break;
                }

                if (useStatement.getName().equals(className)) {
                    isCollision = true;
                }
            }
        }

        return new ImportStatus(isImported, isCollision, importName, importMarker);
    }
}

