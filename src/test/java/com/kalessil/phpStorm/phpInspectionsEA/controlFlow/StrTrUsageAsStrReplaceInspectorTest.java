package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.StrTrUsageAsStrReplaceInspector;

final public class StrTrUsageAsStrReplaceInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/strtr-used-as-strreplace.php");
        myFixture.enableInspections(StrTrUsageAsStrReplaceInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

