package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.dateTime.DateTimeConstantsUsageInspector;

final public class DateTimeConstantsUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/date-time-constants.php");
        myFixture.enableInspections(DateTimeConstantsUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}