package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.NullCoalescingOperatorCanBeUsedInspector;

final public class NullCoalescingOperatorCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsBasicPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        final NullCoalescingOperatorCanBeUsedInspector inspector = new NullCoalescingOperatorCanBeUsedInspector();
        inspector.SUGGEST_SIMPLIFYING_TERNARIES                  = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/lang/null-coalescing-operator.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/lang/null-coalescing-operator.fixed.php");
    }

    public void testIfFindsIfPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        final NullCoalescingOperatorCanBeUsedInspector inspector = new NullCoalescingOperatorCanBeUsedInspector();
        inspector.SUGGEST_SIMPLIFYING_IFS                        = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/lang/null-coalescing-operator.ifs.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/lang/null-coalescing-operator.ifs.fixed.php");
    }
}
