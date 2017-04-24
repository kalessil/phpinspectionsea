package com.kalessil.phpStorm.phpInspectionsEA.utils;

import com.intellij.psi.PsiElement;

final public class Importable {
    private ImportStatus importStatus;
    private String className = null;
    private PsiElement importMarker = null;

    Importable(ImportStatus importStatus, String name, PsiElement importMarker) {
        this.importStatus = importStatus;
        this.className = name;
        this.importMarker = importMarker;
    }

    Importable(ImportStatus importStatus, PsiElement importMarker) {
        this.importStatus = importStatus;
        this.importMarker = importMarker;
    }

    public ImportStatus getImportStatus() {
        return importStatus;
    }

    public String getClassName() {
        return className;
    }

    public PsiElement getImportMarker() {
        return importMarker;
    }
}
