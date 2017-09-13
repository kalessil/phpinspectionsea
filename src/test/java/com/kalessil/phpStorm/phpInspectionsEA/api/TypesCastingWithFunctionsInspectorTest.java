package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.TypesCastingWithFunctionsInspector;

final public class TypesCastingWithFunctionsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new TypesCastingWithFunctionsInspector());
        myFixture.configureByFile("fixtures/api/type-casting-php4-functions.php");
        myFixture.testHighlighting(true, false, true);
    }
}