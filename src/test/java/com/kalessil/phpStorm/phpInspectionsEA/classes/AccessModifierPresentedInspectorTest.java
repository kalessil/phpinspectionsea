package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.AccessModifierPresentedInspector;

final public class AccessModifierPresentedInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/classes/access-modifiers.php");
        myFixture.enableInspections(AccessModifierPresentedInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}