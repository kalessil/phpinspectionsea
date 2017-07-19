package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.SubStrShortHandUsageInspector;

final public class SubStrShortHandUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/substr-short-hand.php");
        myFixture.enableInspections(SubStrShortHandUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}