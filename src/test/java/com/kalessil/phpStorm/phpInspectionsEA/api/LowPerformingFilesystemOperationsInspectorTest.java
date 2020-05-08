package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.LowPerformingFilesystemOperationsInspector;

final public class LowPerformingFilesystemOperationsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final LowPerformingFilesystemOperationsInspector inspector = new LowPerformingFilesystemOperationsInspector();
        inspector.FILE_EXISTS_GUESS                                = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/api/low-performing-filesystem-operations.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/low-performing-filesystem-operations.fixed.php");
    }
}
