package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.DateIntervalSpecificationInspector;

final public class DateIntervalSpecificationInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/pitfalls/date-interval-specification.php");
        myFixture.enableInspections(DateIntervalSpecificationInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
