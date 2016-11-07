package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.TypesCastingWithFunctionsInspector;

public class TypesCastingWithFunctionsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/type-casting-php4-functions.php");
        myFixture.enableInspections(TypesCastingWithFunctionsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}