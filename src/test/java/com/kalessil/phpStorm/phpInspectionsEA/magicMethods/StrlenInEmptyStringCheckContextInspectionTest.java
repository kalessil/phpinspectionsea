package com.kalessil.phpStorm.phpInspectionsEA.magicMethods;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalTransformations.StrlenInEmptyStringCheckContextInspection;
import com.kalessil.phpStorm.phpInspectionsEA.settings.ComparisonStyle;

final public class StrlenInEmptyStringCheckContextInspectionTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        ComparisonStyle.force(ComparisonStyle.REGULAR);

        myFixture.enableInspections(new StrlenInEmptyStringCheckContextInspection());
        myFixture.configureByFile("testData/fixtures/magicMethods/empty-string-comparison.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/magicMethods/empty-string-comparison.fixed.php");
    }
}
