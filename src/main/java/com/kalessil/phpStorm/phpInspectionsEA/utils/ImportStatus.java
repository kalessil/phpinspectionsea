package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;

final public class ImportStatus {
    private Boolean isImported;
    private Boolean isImportNameCollision;
    private String className;
    private PsiElement importMarker;

    ImportStatus(Boolean isImported, Boolean isImportNameCollision, String className, PsiElement importMarker) {
        this.isImported = isImported;
        this.isImportNameCollision = isImportNameCollision;
        this.className = className;
        this.importMarker = importMarker;
    }

    public Boolean getImported() {
        return isImported;
    }

    public Boolean getImportNameCollision() {
        return isImportNameCollision;
    }

    public String getClassName() {
        return className;
    }

    public PsiElement getImportMarker() {
        return importMarker;
    }
}
