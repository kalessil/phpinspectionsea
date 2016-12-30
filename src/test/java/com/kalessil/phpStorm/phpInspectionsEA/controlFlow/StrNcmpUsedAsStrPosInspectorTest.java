package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.StrNcmpUsedAsStrPosInspector;

final public class StrNcmpUsedAsStrPosInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/strncmp-used-as-strpos.php");
        myFixture.enableInspections(StrNcmpUsedAsStrPosInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

