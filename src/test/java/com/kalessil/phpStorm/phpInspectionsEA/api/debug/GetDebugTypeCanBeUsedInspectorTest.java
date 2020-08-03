package com.kalessil.phpStorm.phpInspectionsEA.api.debug;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.debug.GetDebugTypeCanBeUsedInspector;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;

final public class GetDebugTypeCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase
{
    public void testIfFindsAllPatterns() {
        PhpLanguageLevel.set(PhpLanguageLevel.PHP800);
        myFixture.enableInspections(new GetDebugTypeCanBeUsedInspector());
        myFixture.configureByFile("testData/fixtures/api/debug/get_debug_type.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/debug/get_debug_type.fixed.php");
        PhpLanguageLevel.set(null);
    }
}