package com.kalessil.phpStorm.phpInspectionsEA.tests.foreach;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.forEach.DisconnectedForeachInstructionInspector;
import com.kalessil.phpStorm.phpInspectionsEA.tests.utils.FixturesLocationUtil;

public class DisconnectedForeachInstructionInspectorTest extends CodeInsightFixtureTestCase {
    @Override
    protected String getBasePath() {
        return FileUtil.toSystemDependentName(FixturesLocationUtil.RELATIVE_TEST_DATA_PATH);
    }

    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/foreach/disconnected-statements-foreach.php");
        myFixture.enableInspections(DisconnectedForeachInstructionInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/foreach/disconnected-statements-foreach-false-positives.php");
        myFixture.enableInspections(DisconnectedForeachInstructionInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

