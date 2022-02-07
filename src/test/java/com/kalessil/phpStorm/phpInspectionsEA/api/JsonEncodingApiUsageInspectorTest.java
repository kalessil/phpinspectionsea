package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.JsonEncodingApiUsageInspector;

final public class JsonEncodingApiUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsReturnTypePatterns() {
        final JsonEncodingApiUsageInspector inspector = new JsonEncodingApiUsageInspector();
        inspector.HARDEN_DECODING_RESULT_TYPE    = true;
        inspector.DECODE_AS_ARRAY                = true;
        inspector.DECODE_AS_OBJECT               = false;
        inspector.HARDEN_ERRORS_HANDLING         = false;

        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/api/json-encoding-return-type.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/json-encoding-return-type.fixed.php");
    }
    public void testIfFindsErrorsHandlingPatterns() {
        final PhpLanguageLevel level = PhpLanguageLevel.parse("7.3");
        if (level != null && level.getVersionString().equals("7.3")) {
            PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(level);
            final JsonEncodingApiUsageInspector inspector = new JsonEncodingApiUsageInspector();
            inspector.HARDEN_DECODING_RESULT_TYPE = false;
            inspector.HARDEN_ERRORS_HANDLING      = true;

            myFixture.enableInspections(inspector);
            myFixture.configureByFile("testData/fixtures/api/json-encoding-errors-handling.php");
            myFixture.testHighlighting(true, false, true);

            myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
            myFixture.setTestDataPath(".");
            myFixture.checkResultByFile("testData/fixtures/api/json-encoding-errors-handling.fixed.php");
        }
    }
    public void testItRecognizesNameIdentifierFlags() {
        final PhpLanguageLevel level = PhpLanguageLevel.parse("8.0");
        if (level != null && level.getVersionString().equals("8.0")) {
            PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(level);
            final JsonEncodingApiUsageInspector inspector = new JsonEncodingApiUsageInspector();
            inspector.HARDEN_DECODING_RESULT_TYPE = false;
            inspector.HARDEN_ERRORS_HANDLING      = true;

            myFixture.enableInspections(inspector);
            myFixture.configureByFile("testData/fixtures/api/json-encoding-errors-handling.80.php");
            myFixture.testHighlighting(true, false, true);

            myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
            myFixture.setTestDataPath(".");
            myFixture.checkResultByFile("testData/fixtures/api/json-encoding-errors-handling.80.fixed.php");
        }
    }
}
