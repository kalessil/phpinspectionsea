package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.StringNormalizationInspector;

final public class StringNormalizationInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/string-normalization.php");
        myFixture.enableInspections(StringNormalizationInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
