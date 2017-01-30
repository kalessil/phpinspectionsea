package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.UnqualifiedReferenceInspector;

final public class UnqualifiedReferenceInspectorTest extends CodeInsightFixtureTestCase {
    public void testFindsAllNsPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);

        myFixture.configureByFile("fixtures/unqualified-function-refs-ns.php");
        myFixture.enableInspections(UnqualifiedReferenceInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testFindsAllNonNsPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);

        myFixture.configureByFile("fixtures/unqualified-function-refs-no-ns.php");
        myFixture.enableInspections(UnqualifiedReferenceInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
