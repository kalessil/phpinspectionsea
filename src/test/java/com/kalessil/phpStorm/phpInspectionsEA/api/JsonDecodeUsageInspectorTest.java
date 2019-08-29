package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.JsonEncodingApiUsageInspector;

final public class JsonDecodeUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final JsonEncodingApiUsageInspector inspector = new JsonEncodingApiUsageInspector();
        inspector.HARDEN_DECODING_RESULT_TYPE    = true;
        inspector.DECODE_AS_ARRAY                = true;
        inspector.DECODE_AS_OBJECT               = false;
        inspector.HARDEN_ERRORS_HANDLING         = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/api/json-decode.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/json-decode.fixed.php");
    }
}
