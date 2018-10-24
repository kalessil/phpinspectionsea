package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.ClassConstantUsageCorrectnessInspector;

final public class ClassConstantUsageCorrectnessInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ClassConstantUsageCorrectnessInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/class-constant-cases-mismatch.php");
        myFixture.configureByFile("testData/fixtures/pitfalls/class-constant-cases-mismatch.import-and-alias.php");
        myFixture.testHighlighting(true, false, true);
    }
}
