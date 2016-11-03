package com.kalessil.phpStorm.phpInspectionsEA.ifs;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.NotOptimalIfConditionsInspection;

public class NotOptimalIfConditionsInspectionTest extends CodeInsightFixtureTestCase {
    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/ifs/not-optimal-false-positives.php");
        myFixture.enableInspections(NotOptimalIfConditionsInspection.class);
        myFixture.testHighlighting(true, false, true);
    }
}