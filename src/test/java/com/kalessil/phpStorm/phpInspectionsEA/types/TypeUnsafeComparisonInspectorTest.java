package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TypeUnsafeComparisonInspector;

final public class TypeUnsafeComparisonInspectorTest extends CodeInsightFixtureTestCase {
    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/types/type-unsafe-comparison-false-positives.php");
        myFixture.enableInspections(TypeUnsafeComparisonInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
