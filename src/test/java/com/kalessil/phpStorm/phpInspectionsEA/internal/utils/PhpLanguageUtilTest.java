package com.kalessil.phpStorm.phpInspectionsEA.internal.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;

public class PhpLanguageUtilTest extends CodeInsightFixtureTestCase {
    public void testIsNull() {
        Project project = myFixture.getProject();
        PsiElement statement;

        assertFalse(PhpLanguageUtil.isNull(null));

        statement = PhpPsiElementFactory.createFromText(project, PhpReturn.class, "return null;");
        assertFalse(PhpLanguageUtil.isNull(statement));

        statement = PhpPsiElementFactory.createFromText(project, ConstantReference.class, "true");
        assertFalse(PhpLanguageUtil.isNull(statement));

        statement = PhpPsiElementFactory.createFromText(project, ConstantReference.class, "null");
        assertTrue(PhpLanguageUtil.isNull(statement));
    }
}
