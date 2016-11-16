package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.ArrayPushMissUseInspector;

public class ArrayPushMissUseInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/lang/array-push-via-function.php");
        myFixture.enableInspections(ArrayPushMissUseInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
