package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.MagicMethodsValidityInspector;

final public class MagicMethodsValidityInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsInconsistentGetsSetsPatterns() {
        myFixture.configureByFile("fixtures/classes/magic-methods-get-set.php");
        myFixture.enableInspections(MagicMethodsValidityInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsSetStatePatterns() {
        myFixture.configureByFile("fixtures/classes/magic-methods-set-state.php");
        myFixture.enableInspections(MagicMethodsValidityInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testAbstractMethods() {
        myFixture.configureByFile("fixtures/classes/magic-methods-abstract.php");
        myFixture.enableInspections(MagicMethodsValidityInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

