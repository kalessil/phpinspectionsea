package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.InArrayMissUseInspector;

final public class InArrayMissUseInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/in-array-missuse.php");
        myFixture.enableInspections(InArrayMissUseInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}