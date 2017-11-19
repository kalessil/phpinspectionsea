package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.MagicMethodsValidityInspector;

final public class MagicMethodsValidityInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsInconsistentGetsSetsPatterns() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());

        myFixture.configureByFile("fixtures/classes/magicMethods/magic-methods-get-set.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testIfFindsSetStatePatterns() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());

        myFixture.configureByFile("fixtures/classes/magicMethods/magic-methods-set-state.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testAbstractMethods() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());

        myFixture.configureByFile("fixtures/classes/magicMethods/magic-methods-abstract.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testCallsParentMethods() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());

        myFixture.configureByFile("fixtures/classes/magicMethods/magic-methods-calls-parent.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testMissingUnderscoreMethods() {
        // TODO Test not validated for correctness
        myFixture.enableInspections(new MagicMethodsValidityInspector());

        myFixture.configureByFile("fixtures/classes/magicMethods/magic-methods-missing-underscore.php");
        myFixture.testHighlighting(true, false, true);
    }
}

