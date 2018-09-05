package com.kalessil.phpStorm.phpInspectionsEA.ifs;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.ifs.MissingElseKeywordInspector;

final public class MissingElseKeywordInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testFindsAllPatterns() {
        myFixture.enableInspections(new MissingElseKeywordInspector());
        myFixture.configureByFile("testData/fixtures/ifs/missing-else-keyword.php");
        myFixture.testHighlighting(true, false, true);
    }
}
