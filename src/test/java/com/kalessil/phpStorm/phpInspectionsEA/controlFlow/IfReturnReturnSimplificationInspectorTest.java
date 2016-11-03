package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.IfReturnReturnSimplificationInspector;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FixturesLocationUtil;

public class IfReturnReturnSimplificationInspectorTest extends CodeInsightFixtureTestCase {
    @Override
    protected String getBasePath() {
        return FileUtil.toSystemDependentName(FixturesLocationUtil.RELATIVE_TEST_DATA_PATH);
    }

    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/if-return-return-simplify.php");
        myFixture.enableInspections(IfReturnReturnSimplificationInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
