package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem.FopenBinaryUnsafeUsageInspector;

final public class FopenBinaryUnsafeUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new FopenBinaryUnsafeUsageInspector());
        myFixture.configureByFile("testData/fixtures/api/fopen-binary-unsafe.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/fopen-binary-unsafe.fixed.php");
    }
}

