package com.kalessil.phpStorm.phpInspectionsEA.internal.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;

import java.util.HashSet;

final public class PossibleValuesDiscoveryUtilTest extends CodeInsightFixtureTestCase {
    public void testTernaryDiscovery() {
        String pattern        = "$x ? true : false;";
        PsiElement expression = PhpPsiElementFactory.createFromText(myFixture.getProject(), TernaryExpression.class, pattern);
        assertNotNull(expression);

        HashSet<PsiElement> values = PossibleValuesDiscoveryUtil.discover(expression);
        assertEquals(2, values.size());
    }

    public void testFieldReferenceDiscovery() {
        /* let's cover simple case, without complicating test cases */
        String pattern   = "class TestClass { var $property = 'default'; function say(){ echo $this->property; } }";
        PsiElement clazz = PhpPsiElementFactory.createFromText(myFixture.getProject(), PhpClass.class, pattern);
        assertNotNull(clazz);

        PsiElement expression = PsiTreeUtil.findChildOfType(clazz, FieldReference.class);
        assertNotNull(expression);

        HashSet<PsiElement> values = PossibleValuesDiscoveryUtil.discover(expression);
        assertEquals(1, values.size());
        assertInstanceOf(values.iterator().next(), StringLiteralExpression.class);
    }

    public void testOverriddenFieldReferenceDiscovery() {
        /* no default, property overridden with boolean */
    }

    public void testVariableDiscoveryForParamWithDefaultValue() {
        String pattern    = "function testFunction($parameter = false) { return $parameter; }";
        Function callable = PhpPsiElementFactory.createFromText(myFixture.getProject(), Function.class, pattern);
        assertNotNull(callable);

        PsiElement expression = PsiTreeUtil.findChildOfType(callable, GroupStatement.class);
        assertNotNull(expression);
        expression = PsiTreeUtil.findChildOfType(expression, Variable.class);
        assertNotNull(expression);

        HashSet<PsiElement> values = PossibleValuesDiscoveryUtil.discover(expression);
        assertEquals(1, values.size());
        assertInstanceOf(values.iterator().next(), ConstantReference.class);
    }

    public void testVariableDiscoveryForOverriddenVariables() {
        /* no default, property overridden with string => 2 items, 2nd is string literal */
    }
}
