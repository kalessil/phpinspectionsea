package com.kalessil.phpStorm.phpInspectionsEA.magicMethods;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.StrlenInEmptyStringCheckContextInspection;

final public class StrlenInEmptyStringCheckContextInspectionTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new StrlenInEmptyStringCheckContextInspection());

        myFixture.configureByFile("fixtures/magicMethods/empty-string-comparison.php");
        myFixture.testHighlighting(true, false, true);
    }
}
