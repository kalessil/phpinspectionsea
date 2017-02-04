package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.PhpSapiConstantCanBeUsedInspector;

final public class PhpSapiConstantCanBeUsedInspectorTest  extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/php-sapi.php");
        myFixture.enableInspections(PhpSapiConstantCanBeUsedInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}