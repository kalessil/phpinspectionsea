package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.GetClassMissUseInspector;

final public class GetClassMissUseInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new GetClassMissUseInspector());
        myFixture.configureByFile("testData/fixtures/api/get_class.php");
        myFixture.testHighlighting(true, false, true);
    }
}