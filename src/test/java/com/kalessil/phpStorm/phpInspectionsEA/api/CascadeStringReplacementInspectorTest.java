package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.CascadeStringReplacementInspector;

final public class CascadeStringReplacementInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/cascade-str-replace.php");
        myFixture.enableInspections(CascadeStringReplacementInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}