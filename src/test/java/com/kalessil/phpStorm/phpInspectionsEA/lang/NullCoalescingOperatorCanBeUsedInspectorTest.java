package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.NullCoalescingOperatorCanBeUsedInspector;

public class NullCoalescingOperatorCanBeUsedInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);

        myFixture.configureByFile("fixtures/lang/null-coalescing-operator.php");
        myFixture.enableInspections(NullCoalescingOperatorCanBeUsedInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
