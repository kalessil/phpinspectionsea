package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.CaseInsensitiveStringFunctionsMissUseInspector;

public class CaseInsensitiveStringFunctionsMissUseInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/str-i-functions.php");
        myFixture.enableInspections(CaseInsensitiveStringFunctionsMissUseInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}