package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.SubStrUsedAsStrPosInspector;

final public class SubStrUsedAsStrPosInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SubStrUsedAsStrPosInspector());

        myFixture.configureByFile("fixtures/controlFlow/substr-used-as-strpos.php");
        myFixture.testHighlighting(true, false, true);
    }
}

