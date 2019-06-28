package com.kalessil.phpStorm.phpInspectionsEA.regularExpressions;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.regularExpressions.NotOptimalRegularExpressionsInspector;

final public class NotOptimalRegularExpressionsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testFindNotMutuallyExclusiveContiguousQuantifiedTokens() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/not-mutually-exclusive-contiguous-quantified-tokens.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testFindGreedyCharacterSets() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/greedy-character-sets.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testSingleCharactersAlteration() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/single-characters-alteration.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testSuspiciousCharactersRange() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/suspicious-characters-range.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testFindShortCharacterClasses() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/short-character-classes.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testFindBuggyNestedTagsDetection() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/nested-tags.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testDelimiters() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/delimeters.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testProblematicModifiers() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/problematic-modifiers.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testNestedQuantifiers() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/quantifier-compounds-quantifier.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testSenselessIgnoreCaseModifier() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/senseless-i-modifier.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testSenselessDotAllModifier() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/senseless-s-modifier.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testSenselessUngreedyModifier() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/senseless-U-modifier.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testUnnecessaryCaseManipulation() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/unnecessary-case-manipulation.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testMissingUnicodeModifier() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/missing-u-modifier.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testPossibleCtypeUsages() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/ctype-functions-usage.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testPossibleAlternativeRegexFunctionsUsages() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/alternative-regex-functions-usage.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testPossiblePlainApiUsages() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/plain-api-usage.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/regularExpressions/plain-api-usage.fixed.php");
    }
    public void testRegexDiscovery() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/regex-discovery.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testDelimitersSupport() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/regex-delimiters.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testPregMatchPatterns() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/preg-match.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/regularExpressions/preg-match.fixed.php");
    }
    public void testFindMissingDelimiters() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/missing-delimiters.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testSerialClassesCompacting() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/serial-classes-compacting.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testAmbiguousAnythingLeadingAndTrailing() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/ambiguous-anything-leading-trailing.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testAmbiguousDollarEndOnlyModifier() {
        myFixture.enableInspections(new NotOptimalRegularExpressionsInspector());
        myFixture.configureByFile("testData/fixtures/regularExpressions/ambiguous-d-modifier.php");
        myFixture.testHighlighting(true, false, true);
    }
}