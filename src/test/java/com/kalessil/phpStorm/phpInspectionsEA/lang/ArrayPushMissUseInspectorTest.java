package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.ArrayPushMissUseInspector;

final public class ArrayPushMissUseInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final ArrayPushMissUseInspector inspector = new ArrayPushMissUseInspector();
        inspector.REPORT_EXCESSIVE_COUNT_CALLS    = true;

        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/lang/array-push-via-function.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/lang/array-push-via-function.fixed.php");
    }
}
