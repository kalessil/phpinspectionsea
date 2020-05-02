package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.LowPerformingFilesystemsOperationInspector;

final public class LowPerformingDirectoryOperationsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new LowPerformingFilesystemsOperationInspector());
        myFixture.configureByFile("testData/fixtures/api/low-performing-filesystem-operations.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/low-performing-filesystem-operations.fixed.php");
    }
}
