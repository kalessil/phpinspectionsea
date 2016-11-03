package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TypeUnsafeComparisonInspector;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FixturesLocationUtil;

public class TypeUnsafeComparisonInspectorTest extends CodeInsightFixtureTestCase {
    @Override
    protected String getBasePath() {
        return FileUtil.toSystemDependentName(FixturesLocationUtil.RELATIVE_TEST_DATA_PATH);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/types/type-unsafe-comparison-false-positives.php");
        myFixture.enableInspections(TypeUnsafeComparisonInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
