package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.ProperNullCoalescingOperatorUsageInspector;

final public class ProperNullCoalescingOperatorUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP700);
        final ProperNullCoalescingOperatorUsageInspector inspector = new ProperNullCoalescingOperatorUsageInspector();
        inspector.ANALYZE_TYPES                                    = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/codeStyle/proper-null-coalescing-usage.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/codeStyle/proper-null-coalescing-usage.fixed.php");
    }
}
