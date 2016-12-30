package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.StrStrUsedAsStrPosInspector;

final public class StrStrUsedAsStrPosInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/strstr-function.php");
        myFixture.enableInspections(StrStrUsedAsStrPosInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}