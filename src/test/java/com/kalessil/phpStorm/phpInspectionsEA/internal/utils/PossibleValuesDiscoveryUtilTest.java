package com.kalessil.phpStorm.phpInspectionsEA.internal.utils;

import com.intellij.psi.PsiElement;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.TernaryExpression;
import com.kalessil.phpStorm.phpInspectionsEA.utils.PossibleValuesDiscoveryUtil;

import java.util.HashSet;

final public class PossibleValuesDiscoveryUtilTest extends CodeInsightFixtureTestCase {
    public void testTernaryDiscovery() {
        PsiElement expression = PhpPsiElementFactory.createFromText(
                myFixture.getProject(), TernaryExpression.class, "$x ? true : false;"
        );
        assertNotNull(expression);

        HashSet<PsiElement> values = PossibleValuesDiscoveryUtil.discover(expression);
        assertEquals(2, values.size());
    }
}
