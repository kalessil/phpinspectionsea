package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.dateTime.DateTimeSetTimeUsageInspector;

final public class DateTimeSetTimeUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP700);
        myFixture.enableInspections(new DateTimeSetTimeUsageInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/date-time-set-time-milliseconds.php");
        myFixture.testHighlighting(true, false, true);
    }
}
