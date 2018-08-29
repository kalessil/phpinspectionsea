package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.dateTime.DateIntervalSpecificationInspector;

public final class DateIntervalSpecificationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new DateIntervalSpecificationInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/date-interval-specification.php");
        myFixture.testHighlighting(true, false, true);
    }
}
