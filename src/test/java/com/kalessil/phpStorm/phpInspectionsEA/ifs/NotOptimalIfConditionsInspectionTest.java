package com.kalessil.phpStorm.phpInspectionsEA.ifs;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.NotOptimalIfConditionsInspection;

final public class NotOptimalIfConditionsInspectionTest extends PhpCodeInsightFixtureTestCase {
    public void testFalsePositives() {
        myFixture.enableInspections(new NotOptimalIfConditionsInspection());

        myFixture.configureByFile("fixtures/ifs/not-optimal-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testBasicPatterns() {
        NotOptimalIfConditionsInspection inspector = new NotOptimalIfConditionsInspection();
        inspector.SUGGEST_OPTIMIZING_CONDITIONS    = true;
        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/ifs/if-optimal-conditions.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testIssetAndNullComparisonPatterns() {
        NotOptimalIfConditionsInspection inspector = new NotOptimalIfConditionsInspection();
        inspector.REPORT_ISSET_FLAWS               = true;
        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/ifs/if-isset-not-null.php");
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

    public void testMergeIsset() {
        NotOptimalIfConditionsInspection inspector = new NotOptimalIfConditionsInspection();
        inspector.SUGGEST_MERGING_ISSET            = true;
        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/ifs/if-merge-isset.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testInstanceOfFlaws() {
        NotOptimalIfConditionsInspection inspector = new NotOptimalIfConditionsInspection();
        inspector.REPORT_INSTANCE_OF_FLAWS         = true;
        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/ifs/if-instanceof-flaws.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testInstanceOfFalsePositives() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP540);
        NotOptimalIfConditionsInspection inspector = new NotOptimalIfConditionsInspection();
        inspector.REPORT_INSTANCE_OF_FLAWS         = true;
        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/ifs/if-instanceof-flaws-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testIssetFlaws() {
        NotOptimalIfConditionsInspection inspector = new NotOptimalIfConditionsInspection();
        inspector.REPORT_ISSET_FLAWS               = true;
        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/ifs/if-isset-flaws.php");
        myFixture.testHighlighting(true, false, true);
    }
}
