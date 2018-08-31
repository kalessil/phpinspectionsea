package com.kalessil.phpStorm.phpInspectionsEA;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.UnqualifiedReferenceInspector;

final public class UnqualifiedReferenceInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testFindsAllNsPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        final UnqualifiedReferenceInspector inspector = new UnqualifiedReferenceInspector();
        inspector.REPORT_ALL_FUNCTIONS                = true;
        inspector.REPORT_CONSTANTS                    = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/unqualified-function-refs-ns.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/unqualified-function-refs-ns.fixed.php");
    }
    public void testFindsAllNonNsPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        final UnqualifiedReferenceInspector inspector = new UnqualifiedReferenceInspector();
        inspector.REPORT_ALL_FUNCTIONS                = false;
        inspector.REPORT_CONSTANTS                    = false;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/unqualified-function-refs-no-ns.php");
        myFixture.testHighlighting(true, false, true);
    }
}
