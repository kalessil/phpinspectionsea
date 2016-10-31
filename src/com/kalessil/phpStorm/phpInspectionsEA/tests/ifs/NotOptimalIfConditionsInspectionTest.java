package com.kalessil.phpStorm.phpInspectionsEA.tests.ifs;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.NotOptimalIfConditionsInspection;
import com.kalessil.phpStorm.phpInspectionsEA.tests.utils.FixturesLocationUtil;

public class NotOptimalIfConditionsInspectionTest  extends CodeInsightFixtureTestCase {
    @Override
    protected String getBasePath() {
        return FileUtil.toSystemDependentName(FixturesLocationUtil.RELATIVE_TEST_DATA_PATH);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/ifs/not-optimal-false-positives.php");
        myFixture.enableInspections(NotOptimalIfConditionsInspection.class);
        myFixture.testHighlighting(true, false, true);
    }
}