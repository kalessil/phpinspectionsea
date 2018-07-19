package com.kalessil.phpStorm.phpInspectionsEA.phpUnit;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.ClassMockingCorrectnessInspector;

final public class ClassMockingCorrectnessInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ClassMockingCorrectnessInspector());
        myFixture.configureByFile("fixtures/pitfalls/class-mocking-correctness.php");
        myFixture.testHighlighting(true, false, true);
    }
}
