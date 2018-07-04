package com.kalessil.phpStorm.phpInspectionsEA.regularExpressions;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.NotOptimalRegularExpressionsInspector;

final public class NotOptimalRegularExpressionsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testFindNotMutuallyExclusiveContiguousQuantifiedTokens() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("fixtures/regularExpressions/not-mutually-exclusive-contiguous-quantified-tokens.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testFindGreedyCharacterSets() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("fixtures/regularExpressions/greedy-character-sets.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testSingleCharactersAlteration() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("fixtures/regularExpressions/single-characters-alteration.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testSuspiciousCharactersRange() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("fixtures/regularExpressions/suspicious-characters-range.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testProblematicModifiers() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("fixtures/regularExpressions/problematic-modifiers.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testNestedQuantifiers() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("fixtures/regularExpressions/quantifier-compounds-quantifier.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testSenselessIgnoreCaseModifier() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("fixtures/regularExpressions/senseless-i-modifier.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testUnnecessaryCaseManipulation() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("fixtures/regularExpressions/unnecessary-case-manipulation.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testMissingUnicodeModifier() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("fixtures/regularExpressions/missing-u-modifier.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testPossibleCtypeUsages() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("fixtures/regularExpressions/ctype-functions-usage.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testPossiblePlainApiUsages() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("fixtures/regularExpressions/plain-api-usage.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/regularExpressions/plain-api-usage.fixed.php");
    }
    public void testRegexDiscovery() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("fixtures/regularExpressions/regex-discovery.php");
        myFixture.testHighlighting(true, false, true);
    }
}