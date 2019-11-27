package com.kalessil.phpStorm.phpInspectionsEA.api.strings;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.UriPartExtractionInspector;

final public class UriPartExtractionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UriPartExtractionInspector());
        myFixture.configureByFile("testData/fixtures/api/strings/uri-part-extraction.php");
        myFixture.testHighlighting(true, false, true);
    }
}
