package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.CaseInsensitiveStringFunctionsMissUseInspector;

final public class CaseInsensitiveStringFunctionsMissUseInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CaseInsensitiveStringFunctionsMissUseInspector());

        myFixture.configureByFile("fixtures/api/str-i-functions.php");
        myFixture.testHighlighting(true, false, true);
    }
}