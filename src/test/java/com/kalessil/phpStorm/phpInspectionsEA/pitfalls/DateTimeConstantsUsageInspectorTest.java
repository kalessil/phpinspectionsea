package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.DateTimeConstantsUsageInspector;

final public class DateTimeConstantsUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/date-time-constants.php");
        myFixture.enableInspections(DateTimeConstantsUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}