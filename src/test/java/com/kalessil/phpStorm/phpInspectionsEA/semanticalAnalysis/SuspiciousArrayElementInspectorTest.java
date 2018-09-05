package com.kalessil.phpStorm.phpInspectionsEA.semanticalAnalysis;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SuspiciousArrayElementInspector;

final public class SuspiciousArrayElementInspectorTest extends PhpCodeInsightFixtureTestCase {
public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SuspiciousArrayElementInspector());
        myFixture.configureByFile("testData/fixtures/semanticalAnalysis/suspicious-array-element.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/semanticalAnalysis/suspicious-array-element.fixed.php");
    }
}
