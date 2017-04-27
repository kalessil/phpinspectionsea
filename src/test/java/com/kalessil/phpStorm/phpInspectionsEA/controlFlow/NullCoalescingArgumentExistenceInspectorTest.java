package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.NullCoalescingArgumentExistenceInspector;

final public class NullCoalescingArgumentExistenceInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        myFixture.enableInspections(NullCoalescingArgumentExistenceInspector.class);

        myFixture.configureByFile("fixtures/controlFlow/null-coalescing-variable-existence.php");
        myFixture.testHighlighting(true, false, true);
    }
}