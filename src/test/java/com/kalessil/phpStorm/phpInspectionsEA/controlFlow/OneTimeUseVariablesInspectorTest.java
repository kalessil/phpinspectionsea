package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.OneTimeUseVariablesInspector;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FixturesLocationUtil;

public class OneTimeUseVariablesInspectorTest extends CodeInsightFixtureTestCase {
    @Override
    protected String getBasePath() {
        return FileUtil.toSystemDependentName(FixturesLocationUtil.RELATIVE_TEST_DATA_PATH);
    }

    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/one-time-use-variables.php");
        myFixture.enableInspections(OneTimeUseVariablesInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/one-time-use-variables-false-positives.php");
        myFixture.enableInspections(OneTimeUseVariablesInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
