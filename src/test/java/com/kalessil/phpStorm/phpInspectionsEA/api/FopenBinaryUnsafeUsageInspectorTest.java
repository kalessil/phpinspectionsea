package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.FopenBinaryUnsafeUsageInspector;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FixturesLocationUtil;

public class FopenBinaryUnsafeUsageInspectorTest extends CodeInsightFixtureTestCase {
    @Override
    protected String getBasePath() {
        return FileUtil.toSystemDependentName(FixturesLocationUtil.RELATIVE_TEST_DATA_PATH);
    }

    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/fopen-binary-unsafe.php");
        myFixture.enableInspections(FopenBinaryUnsafeUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

