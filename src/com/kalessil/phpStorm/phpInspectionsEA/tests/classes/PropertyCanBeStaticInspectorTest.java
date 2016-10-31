package com.kalessil.phpStorm.phpInspectionsEA.tests.classes;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.PropertyCanBeStaticInspector;
import com.kalessil.phpStorm.phpInspectionsEA.tests.utils.FixturesLocationUtil;

public class PropertyCanBeStaticInspectorTest  extends CodeInsightFixtureTestCase {
    @Override
    protected String getBasePath() {
        return FileUtil.toSystemDependentName(FixturesLocationUtil.RELATIVE_TEST_DATA_PATH);
    }

    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/classes/property-can-be-static.php");
        myFixture.enableInspections(PropertyCanBeStaticInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/classes/property-can-be-static-false-positives.php");
        myFixture.enableInspections(PropertyCanBeStaticInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}