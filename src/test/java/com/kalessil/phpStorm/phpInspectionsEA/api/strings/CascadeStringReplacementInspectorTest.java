package com.kalessil.phpStorm.phpInspectionsEA.api.strings;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.CascadeStringReplacementInspector;

final public class CascadeStringReplacementInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final CascadeStringReplacementInspector inspector = new CascadeStringReplacementInspector();
        inspector.USE_SHORT_ARRAYS_SYNTAX                 = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/api/strings/cascade-str-replace.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/strings/cascade-str-replace.fixed.php");
    }
}
