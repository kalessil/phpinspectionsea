package com.kalessil.phpStorm.phpInspectionsEA.regularExpressions;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.NotOptimalRegularExpressionsInspector;

final public class NotOptimalRegularExpressionsInspectorTest extends CodeInsightFixtureTestCase {
    public void testFindGreedyCharacterSets() {
        myFixture.configureByFile("fixtures/regularExpressions/greedy-character-sets.php");
        myFixture.enableInspections(NotOptimalRegularExpressionsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testProblematicModifiers() {
        myFixture.configureByFile("fixtures/regularExpressions/problematic-modifiers.php");
        myFixture.enableInspections(NotOptimalRegularExpressionsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testNestedQuantifiers() {
        myFixture.configureByFile("fixtures/regularExpressions/quantifier-compounds-quantifier.php");
        myFixture.enableInspections(NotOptimalRegularExpressionsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testSenselessIgnoreCaseModifier() {
        myFixture.configureByFile("fixtures/regularExpressions/senseless-i-modifier.php");
        myFixture.enableInspections(NotOptimalRegularExpressionsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testMissingUnicodeModifier() {
        myFixture.configureByFile("fixtures/regularExpressions/missing-u-modifier.php");
        myFixture.enableInspections(NotOptimalRegularExpressionsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testPossibleCtypeUsages() {
        myFixture.configureByFile("fixtures/regularExpressions/ctype-functions-usage.php");
        myFixture.enableInspections(NotOptimalRegularExpressionsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}