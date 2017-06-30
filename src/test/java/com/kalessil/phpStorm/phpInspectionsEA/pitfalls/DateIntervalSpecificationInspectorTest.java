package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.dateTime.DateIntervalSpecificationInspector;

public final class DateIntervalSpecificationInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new DateIntervalSpecificationInspector());
        myFixture.configureByFile("fixtures/pitfalls/date-interval-specification.php");
        myFixture.testHighlighting(true, false, true);
    }
}
