package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.DirnameCallOnFileConstantInspector;

final public class DirnameCallOnFileConstantInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/dirname-on-file-const.php");
        myFixture.enableInspections(DirnameCallOnFileConstantInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}