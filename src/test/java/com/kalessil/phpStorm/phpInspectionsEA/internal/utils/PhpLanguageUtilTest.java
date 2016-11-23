package com.kalessil.phpStorm.phpInspectionsEA.internal.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.ConstantReference;
import com.jetbrains.php.lang.psi.elements.PhpReturn;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PhpLanguageUtil;

final public class PhpLanguageUtilTest extends CodeInsightFixtureTestCase {
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

    public void testIsTrue() {
        Project project = myFixture.getProject();
        PsiElement statement;

        assertFalse(PhpLanguageUtil.isTrue(null));

        statement = PhpPsiElementFactory.createFromText(project, PhpReturn.class, "return null;");
        assertFalse(PhpLanguageUtil.isTrue(statement));
        statement = PhpPsiElementFactory.createFromText(project, ConstantReference.class, "null");
        assertFalse(PhpLanguageUtil.isTrue(statement));

        statement = PhpPsiElementFactory.createFromText(project, ConstantReference.class, "true");
        assertTrue(PhpLanguageUtil.isTrue(statement));
    }

    public void testIsFalse() {
        Project project = myFixture.getProject();
        PsiElement statement;

        assertFalse(PhpLanguageUtil.isFalse(null));

        statement = PhpPsiElementFactory.createFromText(project, PhpReturn.class, "return null;");
        assertFalse(PhpLanguageUtil.isFalse(statement));
        statement = PhpPsiElementFactory.createFromText(project, ConstantReference.class, "null");
        assertFalse(PhpLanguageUtil.isFalse(statement));

        statement = PhpPsiElementFactory.createFromText(project, ConstantReference.class, "false");
        assertTrue(PhpLanguageUtil.isFalse(statement));
    }

    public void testIsBoolean() {
        Project project = myFixture.getProject();
        PsiElement statement;

        assertFalse(PhpLanguageUtil.isBoolean(null));

        statement = PhpPsiElementFactory.createFromText(project, PhpReturn.class, "return null;");
        assertFalse(PhpLanguageUtil.isBoolean(statement));
        statement = PhpPsiElementFactory.createFromText(project, ConstantReference.class, "null");
        assertFalse(PhpLanguageUtil.isBoolean(statement));

        statement = PhpPsiElementFactory.createFromText(project, ConstantReference.class, "false");
        assertTrue(PhpLanguageUtil.isBoolean(statement));
        statement = PhpPsiElementFactory.createFromText(project, ConstantReference.class, "true");
        assertTrue(PhpLanguageUtil.isBoolean(statement));
    }
}
