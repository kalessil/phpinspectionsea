package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.ArraySearchUsedAsInArrayInspector;

final public class ArraySearchUsedAsInArrayInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/array-search-used-as-in-array.php");
        myFixture.enableInspections(ArraySearchUsedAsInArrayInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
