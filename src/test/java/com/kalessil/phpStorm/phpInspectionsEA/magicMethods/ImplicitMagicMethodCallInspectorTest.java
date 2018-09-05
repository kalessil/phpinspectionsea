package com.kalessil.phpStorm.phpInspectionsEA.magicMethods;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.ImplicitMagicMethodCallInspector;

final public class ImplicitMagicMethodCallInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final ImplicitMagicMethodCallInspector inspector = new ImplicitMagicMethodCallInspector();
        inspector.SUGGEST_USING_STRING_CASTING           = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/magicMethods/magic-methods-implicit-call.php");
        myFixture.testHighlighting(true, false, true);
    }
}
