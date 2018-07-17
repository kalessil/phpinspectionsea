package com.kalessil.phpStorm.phpInspectionsEA.phpUnit;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.MockingMethodsCorrectnessInspector;

final public class MockingMethodsCorrectnessInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsPatterns() {
        myFixture.enableInspections(new MockingMethodsCorrectnessInspector());
        myFixture.configureByFile("fixtures/phpUnit/mocking-methods.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/phpUnit/mocking-methods.fixed.php");
    }
}
