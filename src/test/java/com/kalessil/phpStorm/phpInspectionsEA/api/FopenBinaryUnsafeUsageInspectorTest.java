package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem.FopenBinaryUnsafeUsageInspector;

final public class FopenBinaryUnsafeUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/fopen-binary-unsafe.php");
        myFixture.enableInspections(FopenBinaryUnsafeUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

