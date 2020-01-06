package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.magicMethods.MagicMethodsValidityInspector;

final public class MagicMethodsValidityInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsInconsistentGetsSetsPatterns() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());
        myFixture.configureByFile("testData/fixtures/classes/magicMethods/magic-methods-get-set.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsSetStatePatterns() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());
        myFixture.configureByFile("testData/fixtures/classes/magicMethods/magic-methods-set-state.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testAbstractMethods() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());
        myFixture.configureByFile("testData/fixtures/classes/magicMethods/magic-methods-abstract.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testCallsParentMethods() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());
        myFixture.configureByFile("testData/fixtures/classes/magicMethods/magic-methods-calls-parent.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testToStringMethod() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());
        myFixture.configureByFile("testData/fixtures/classes/magicMethods/magic-methods-toString.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testAutoloadMethod() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());
        myFixture.configureByFile("testData/fixtures/classes/magicMethods/magic-methods-autoload.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testCloneDestructMethod() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());
        myFixture.configureByFile("testData/fixtures/classes/magicMethods/magic-methods-clone-destruct.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testCallStaticMethod() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());
        myFixture.configureByFile("testData/fixtures/classes/magicMethods/magic-methods-call-static.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testInvokeMethod() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());
        myFixture.configureByFile("testData/fixtures/classes/magicMethods/magic-methods-invoke.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testDebugInfoMethod() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());
        myFixture.configureByFile("testData/fixtures/classes/magicMethods/magic-methods-debug-info.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testWakeUpMethod() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());
        myFixture.configureByFile("testData/fixtures/classes/magicMethods/magic-methods-wake-up.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testUnserializeMethod() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());
        myFixture.configureByFile("testData/fixtures/classes/magicMethods/magic-methods-unserialize.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testSleepMethod() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());
        myFixture.configureByFile("testData/fixtures/classes/magicMethods/magic-methods-sleep.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testSerializeMethod() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());
        myFixture.configureByFile("testData/fixtures/classes/magicMethods/magic-methods-serialize.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testMissingUnderscoreMethods() {
        myFixture.enableInspections(new MagicMethodsValidityInspector());
        myFixture.configureByFile("testData/fixtures/classes/magicMethods/magic-methods-missing-underscore.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/classes/magicMethods/magic-methods-missing-underscore.fixed.php");
    }
}

