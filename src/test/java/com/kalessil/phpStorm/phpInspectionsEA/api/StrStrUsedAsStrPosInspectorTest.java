package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.StrStrUsedAsStrPosInspector;

final public class StrStrUsedAsStrPosInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new StrStrUsedAsStrPosInspector());

        myFixture.configureByFile("fixtures/api/strstr-function.php");
        myFixture.testHighlighting(true, false, true);
    }
}