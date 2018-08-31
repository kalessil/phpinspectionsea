package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.CompactCanBeUsedInspector;

final public class CompactCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CompactCanBeUsedInspector());
        myFixture.configureByFile("testData/fixtures/lang/compact.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/lang/compact.fixed.php");
    }
}
