package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.GetTypeMissUseInspector;

final public class GetTypeMissUseInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new GetTypeMissUseInspector());

        myFixture.configureByFile("fixtures/api/gettype.php");
        myFixture.testHighlighting(true, false, true);
    }
}