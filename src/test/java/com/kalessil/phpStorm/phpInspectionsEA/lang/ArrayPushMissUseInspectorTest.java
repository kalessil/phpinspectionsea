package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.ArrayPushMissUseInspector;

final public class ArrayPushMissUseInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ArrayPushMissUseInspector());

        myFixture.configureByFile("fixtures/lang/array-push-via-function.php");
        myFixture.testHighlighting(true, false, true);
    }
}
