package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.dateTime.DateTimeConstantsUsageInspector;

final public class DateTimeConstantsUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new DateTimeConstantsUsageInspector());
        myFixture.configureByFile("fixtures/pitfalls/date-time-constants.php");
        myFixture.testHighlighting(true, false, true);
    }
}