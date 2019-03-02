package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem.BasenameCallsContextInspector;

final public class BasenameCallsContextInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new BasenameCallsContextInspector());
        myFixture.configureByFile("testData/fixtures/api/basename-context.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/array/basename-context.fixed.php");
    }
}