package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.AccessModifierPresentedInspector;

final public class AccessModifierPresentedInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        AccessModifierPresentedInspector inspector = new AccessModifierPresentedInspector();
        inspector.ANALYZE_INTERFACES               = true;

        myFixture.configureByFile("fixtures/classes/access-modifiers.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
}