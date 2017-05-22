package com.kalessil.phpStorm.phpInspectionsEA.ifs;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.NotOptimalIfConditionsInspection;

final public class NotOptimalIfConditionsInspectionTest extends CodeInsightFixtureTestCase {
    public void testFalsePositives() {
        myFixture.enableInspections(new NotOptimalIfConditionsInspection());

        myFixture.configureByFile("fixtures/ifs/not-optimal-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIssetAndNullComparisonPatterns() {
        myFixture.enableInspections(new NotOptimalIfConditionsInspection());

        myFixture.configureByFile("fixtures/ifs/if-isset-not-null.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testInlineBooleansPatterns() {
        myFixture.enableInspections(new NotOptimalIfConditionsInspection());

        myFixture.configureByFile("fixtures/ifs/if-inline-booleans.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testLiteralOperatorsPatterns() {
        NotOptimalIfConditionsInspection inspector = new NotOptimalIfConditionsInspection();
        inspector.REPORT_LITERAL_OPERATORS         = true;

        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/ifs/if-literal-operators.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testDuplicateConditions() {
        NotOptimalIfConditionsInspection inspector = new NotOptimalIfConditionsInspection();
        inspector.REPORT_DUPLICATE_CONDITIONS      = true;

        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/ifs/if-duplicate-conditions.php");
        myFixture.testHighlighting(true, false, true);
    }
}
