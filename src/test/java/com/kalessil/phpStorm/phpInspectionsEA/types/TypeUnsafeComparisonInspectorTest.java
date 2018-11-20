package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TypeUnsafeComparisonInspector;

final public class TypeUnsafeComparisonInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new TypeUnsafeComparisonInspector());
        myFixture.configureByFile("testData/fixtures/types/type-unsafe-comparison.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/types/type-unsafe-comparison.fixed.php");
    }
    public void testFalsePositives() {
        myFixture.enableInspections(new TypeUnsafeComparisonInspector());
        myFixture.configureByFile("testData/fixtures/types/type-unsafe-comparison-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
}
