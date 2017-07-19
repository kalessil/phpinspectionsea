package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.CascadeStringReplacementInspector;

final public class CascadeStringReplacementInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CascadeStringReplacementInspector());

        myFixture.configureByFile("fixtures/api/cascade-str-replace.php");
        myFixture.testHighlighting(true, false, true);
    }
}