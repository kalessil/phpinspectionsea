package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.UnqualifiedFunctionReferenceInspector;

final public class UnqualifiedFunctionReferenceInspectorTest extends CodeInsightFixtureTestCase {
    public void testFindsAllNsPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);

        myFixture.configureByFile("fixtures/unqualified-function-refs-ns.php");
        myFixture.enableInspections(UnqualifiedFunctionReferenceInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testFindsAllNonNsPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);

        myFixture.configureByFile("fixtures/unqualified-function-refs-no-ns.php");
        myFixture.enableInspections(UnqualifiedFunctionReferenceInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
