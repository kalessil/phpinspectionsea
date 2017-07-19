package com.kalessil.phpStorm.phpInspectionsEA.deadCode;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.UselessUnsetInspector;

final public class UselessUnsetInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/deadCode/useless-unset.php");
        myFixture.enableInspections(UselessUnsetInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}