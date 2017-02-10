package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.ExplodeMissUseInspector;

final public class ExplodeMissUseInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/explode-missuse.php");
        myFixture.enableInspections(ExplodeMissUseInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

