package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.DeprecatedIniOptionsInspector;

final public class DeprecatedIniOptionsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new DeprecatedIniOptionsInspector());
        myFixture.configureByFile("testData/fixtures/api/deprecated-ini-options.php");
        myFixture.testHighlighting(true, false, true);
    }
}
