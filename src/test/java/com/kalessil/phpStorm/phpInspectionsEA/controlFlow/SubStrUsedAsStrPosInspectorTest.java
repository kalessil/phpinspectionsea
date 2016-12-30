package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.SubStrUsedAsStrPosInspector;

final public class SubStrUsedAsStrPosInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/substr-used-as-strpos.php");
        myFixture.enableInspections(SubStrUsedAsStrPosInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

