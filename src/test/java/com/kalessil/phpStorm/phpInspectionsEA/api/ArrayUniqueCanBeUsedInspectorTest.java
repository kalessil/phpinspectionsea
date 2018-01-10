package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.ArrayUniqueCanBeUsedInspector;

final public class ArrayUniqueCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        /* nearest PHP 7.2 environment set up */
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        ArrayUniqueCanBeUsedInspector inspector = new ArrayUniqueCanBeUsedInspector();
        inspector.FORCE_ANALYSIS                = true;
        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/api/array-unique.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/api/array-unique.fixed.php");
    }
}