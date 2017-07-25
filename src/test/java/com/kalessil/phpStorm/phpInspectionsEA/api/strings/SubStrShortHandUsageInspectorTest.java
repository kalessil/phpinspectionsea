package com.kalessil.phpStorm.phpInspectionsEA.api.strings;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.SubStrShortHandUsageInspector;

final public class SubStrShortHandUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SubStrShortHandUsageInspector());

        myFixture.configureByFile("fixtures/api/strings/substr-short-hand.php");
        myFixture.testHighlighting(true, false, true);
    }
}