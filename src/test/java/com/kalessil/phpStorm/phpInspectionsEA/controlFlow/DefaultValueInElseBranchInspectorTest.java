package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.DefaultValueInElseBranchInspector;

public class DefaultValueInElseBranchInspectorTest  extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/default-value-in-else.php");
        myFixture.enableInspections(DefaultValueInElseBranchInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}