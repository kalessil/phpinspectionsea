package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem.FopenBinaryUnsafeUsageInspector;

final public class FopenBinaryUnsafeUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/fopen-binary-unsafe.php");
        myFixture.enableInspections(FopenBinaryUnsafeUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

