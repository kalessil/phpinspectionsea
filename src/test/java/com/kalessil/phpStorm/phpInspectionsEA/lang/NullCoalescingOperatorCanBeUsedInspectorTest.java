package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.NullCoalescingOperatorCanBeUsedInspector;

final public class NullCoalescingOperatorCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        myFixture.enableInspections(new NullCoalescingOperatorCanBeUsedInspector());
        myFixture.configureByFile("testData/fixtures/lang/null-coalescing-operator.php");
        myFixture.testHighlighting(true, false, true);
    }
}
