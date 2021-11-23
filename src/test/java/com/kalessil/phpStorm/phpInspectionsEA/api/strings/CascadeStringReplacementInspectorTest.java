package com.kalessil.phpStorm.phpInspectionsEA.api.strings;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.CascadeStringReplacementInspector;

final public class CascadeStringReplacementInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final CascadeStringReplacementInspector inspector = new CascadeStringReplacementInspector();
        inspector.USE_SHORT_ARRAYS_SYNTAX                 = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/api/strings/cascade-str-replace.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/strings/cascade-str-replace.fixed.php");
    }

    public void testIfFindsPhp74Patterns() {
        final PhpLanguageLevel level = PhpLanguageLevel.parse("7.4");
        if (level != null && level.getVersionString().equals("7.4")) {
            PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(level);
            final CascadeStringReplacementInspector inspector = new CascadeStringReplacementInspector();
            inspector.USE_SHORT_ARRAYS_SYNTAX = true;
            myFixture.enableInspections(inspector);
            myFixture.configureByFile("testData/fixtures/api/strings/cascade-str-replace.74.php");
            myFixture.testHighlighting(true, false, true);

            myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
            myFixture.setTestDataPath(".");
            myFixture.checkResultByFile("testData/fixtures/api/strings/cascade-str-replace.74.fixed.php");
        }
    }
}
