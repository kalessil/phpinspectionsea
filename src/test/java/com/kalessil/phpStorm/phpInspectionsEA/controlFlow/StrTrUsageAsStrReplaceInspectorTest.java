package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.StrTrUsageAsStrReplaceInspector;

final public class StrTrUsageAsStrReplaceInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/strtr-used-as-strreplace.php");
        myFixture.enableInspections(StrTrUsageAsStrReplaceInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

