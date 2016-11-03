package com.kalessil.phpStorm.phpInspectionsEA.foreach;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.AlterInForeachInspector;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FixturesLocationUtil;

public class AlterInForeachInspectorTest extends CodeInsightFixtureTestCase {
    @Override
    protected String getBasePath() {
        return FileUtil.toSystemDependentName(FixturesLocationUtil.RELATIVE_TEST_DATA_PATH);
    }

    public void testIfFindsAllPatterns() {
        AlterInForeachInspector inspector    = new AlterInForeachInspector();
        inspector.SUGGEST_USING_VALUE_BY_REF = true;

        myFixture.configureByFile("fixtures/foreach/alter-in-foreach.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/foreach/alter-in-foreach-false-positives.php");
        myFixture.enableInspections(AlterInForeachInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
