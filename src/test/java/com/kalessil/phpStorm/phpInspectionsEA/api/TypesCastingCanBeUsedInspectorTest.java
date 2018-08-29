package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.TypesCastingCanBeUsedInspector;

final public class TypesCastingCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final TypesCastingCanBeUsedInspector inspector = new TypesCastingCanBeUsedInspector();
        inspector.REPORT_INLINES                       = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/api/type-casting-can-be-used.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/type-casting-can-be-used.fixed.php");
    }
}
