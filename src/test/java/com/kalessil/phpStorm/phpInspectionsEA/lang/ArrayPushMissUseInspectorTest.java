package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.ArrayPushMissUseInspector;

final public class ArrayPushMissUseInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ArrayPushMissUseInspector());
        myFixture.configureByFile("testData/fixtures/lang/array-push-via-function.php");
        myFixture.testHighlighting(true, false, true);
    }
}
