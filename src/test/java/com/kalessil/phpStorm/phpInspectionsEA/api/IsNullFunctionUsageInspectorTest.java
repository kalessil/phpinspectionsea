package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.IsNullFunctionUsageInspector;

final public class IsNullFunctionUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/is-null-function.php");
        myFixture.enableInspections(IsNullFunctionUsageInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}