package com.kalessil.phpStorm.phpInspectionsEA.internal.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;

import java.util.Set;

final public class PossibleValuesDiscoveryUtilTest extends PhpCodeInsightFixtureTestCase {
    public void testTernaryDiscovery() {
        String pattern        = "$x ? true : false;";
        PsiElement expression = PhpPsiElementFactory.createFromText(myFixture.getProject(), TernaryExpression.class, pattern);
        assertNotNull(expression);

        Set<PsiElement> values = PossibleValuesDiscoveryUtil.discover(expression);
        assertEquals(2, values.size());
        assertTrue(values.stream().allMatch(variant -> variant instanceof ConstantReference));
    }

    public void testNullCoalescDiscovery() {
        String pattern        = "$x ?? false;";
        PsiElement expression = PhpPsiElementFactory.createFromText(myFixture.getProject(), BinaryExpression.class, pattern);
        assertNotNull(expression);

        Set<PsiElement> values = PossibleValuesDiscoveryUtil.discover(expression);
        assertEquals(1, values.size());
        assertTrue(values.stream().allMatch(variant -> variant instanceof ConstantReference));
    }

    public void testClassFieldReferenceDiscovery() {
        /* let's cover simple case, without complicating test cases */
        String pattern   =
                "class test { " +
                    "var $property = 'default'; " +
                    "function __construct() { " +
                        "$this->property = false; " +
                    " } " +
                    "function say(){ " +
                        "return $this->property; " +
                    "} " +
                " }";
        Function callable = PhpPsiElementFactory.createFromText(myFixture.getProject(), Function.class, pattern);
        assertNotNull(callable);

        PsiElement expression = PsiTreeUtil.findChildOfType(callable, PhpReturn.class);
        assertNotNull(expression);
        expression = PsiTreeUtil.findChildOfType(expression, FieldReference.class);
        assertNotNull(expression);

        Set<PsiElement> values  = PossibleValuesDiscoveryUtil.discover(expression);
        assertEquals(2, values.size());
        assertTrue(values.stream().anyMatch(variant -> variant instanceof StringLiteralExpression));
        assertTrue(values.stream().anyMatch(variant -> variant instanceof ConstantReference));
    }

    public void testClassConstantReferenceDiscovery() {
        String pattern   = "class test { const PROPERTY = 'default'; function say(){ echo self::PROPERTY; } }";
        PsiElement clazz = PhpPsiElementFactory.createFromText(myFixture.getProject(), PhpClass.class, pattern);
        assertNotNull(clazz);

        PsiElement expression = PsiTreeUtil.findChildOfType(clazz, ClassConstantReference.class);
        assertNotNull(expression);

        Set<PsiElement> values = PossibleValuesDiscoveryUtil.discover(expression);
        assertEquals(1, values.size());
        assertInstanceOf(values.iterator().next(), StringLiteralExpression.class);
    }

    public void testOverriddenFieldReferenceDiscovery() {
        String pattern   =
                "class test { " +
                    "var $x; " +
                    "function test(){ " +
                        "$this->x = 'default'; " +
                        "$this->x = $y = 'default'; " +
                        "$this->x .= 0; " +
                        "list($this->x, $y) = [0, 0]; " +
                        "return $this->x; " +
                    "} " +
                " }";
        Function callable = PhpPsiElementFactory.createFromText(myFixture.getProject(), Function.class, pattern);
        assertNotNull(callable);

        PsiElement expression = PsiTreeUtil.findChildOfType(callable, PhpReturn.class);
        assertNotNull(expression);
        expression = PsiTreeUtil.findChildOfType(expression, FieldReference.class);
        assertNotNull(expression);

        Set<PsiElement> values  = PossibleValuesDiscoveryUtil.discover(expression);
        assertEquals(2, values.size());
        assertInstanceOf(values.iterator().next(), StringLiteralExpression.class);
    }

    public void testVariableDiscoveryForParamWithDefaultValue() {
        String pattern    = "function test($parameter = false) { return $parameter; }";
        Function callable = PhpPsiElementFactory.createFromText(myFixture.getProject(), Function.class, pattern);
        assertNotNull(callable);

        PsiElement expression = PsiTreeUtil.findChildOfType(callable, GroupStatement.class);
        assertNotNull(expression);
        expression = PsiTreeUtil.findChildOfType(expression, Variable.class);
        assertNotNull(expression);

        Set<PsiElement> values = PossibleValuesDiscoveryUtil.discover(expression);
        assertEquals(1, values.size());
        assertInstanceOf(values.iterator().next(), ConstantReference.class);
    }

    public void testVariableDiscoveryForOverriddenVariables() {
        String pattern    = "function test() { $x = null; $x = $y = '...'; $x .= 0; list($x, $y) = [0, 0]; return $x; }";
        Function callable = PhpPsiElementFactory.createFromText(myFixture.getProject(), Function.class, pattern);
        assertNotNull(callable);

        PsiElement expression = PsiTreeUtil.findChildOfType(callable, PhpReturn.class);
        assertNotNull(expression);
        expression = PsiTreeUtil.findChildOfType(expression, Variable.class);
        assertNotNull(expression);

        Set<PsiElement> values = PossibleValuesDiscoveryUtil.discover(expression);
        assertEquals(2, values.size());
        assertTrue(values.stream().anyMatch(variant -> variant instanceof StringLiteralExpression));
        assertTrue(values.stream().anyMatch(variant -> variant instanceof ConstantReference));
    }
}
