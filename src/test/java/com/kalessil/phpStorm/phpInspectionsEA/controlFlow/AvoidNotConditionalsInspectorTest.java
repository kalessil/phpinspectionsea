package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.AvoidNotConditionalsInspector;

final public class AvoidNotConditionalsInspectorTest extends CodeInsightFixtureTestCase {
    public void testThatWeCanAvoidNotOperatorOnIfs() {
        myFixture.configureByFile("fixtures/ifs/if-avoid-not-operator.php");
        myFixture.enableInspections(AvoidNotConditionalsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

