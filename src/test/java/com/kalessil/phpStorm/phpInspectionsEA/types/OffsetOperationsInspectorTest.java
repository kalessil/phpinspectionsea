package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.OffsetOperationsInspector;

final public class OffsetOperationsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/types/offset-operations.php");
        myFixture.enableInspections(OffsetOperationsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}