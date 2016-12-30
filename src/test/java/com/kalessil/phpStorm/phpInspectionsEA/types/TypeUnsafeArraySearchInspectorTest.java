package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.TypeUnsafeArraySearchInspector;

final public class TypeUnsafeArraySearchInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/strict-array-search.php");
        myFixture.enableInspections(TypeUnsafeArraySearchInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
