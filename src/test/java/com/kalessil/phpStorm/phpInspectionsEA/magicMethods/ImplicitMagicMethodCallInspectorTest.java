package com.kalessil.phpStorm.phpInspectionsEA.magicMethods;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.ImplicitMagicMethodCallInspector;

final public class ImplicitMagicMethodCallInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        ImplicitMagicMethodCallInspector inspector = new ImplicitMagicMethodCallInspector();
        inspector.SUGGEST_USING_STRING_CASTING     = true;

        myFixture.configureByFile("fixtures/magicMethods/magic-methods-implicit-call.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
}
