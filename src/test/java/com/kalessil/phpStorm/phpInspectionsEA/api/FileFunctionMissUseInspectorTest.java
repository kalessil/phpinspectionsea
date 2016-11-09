package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.FileFunctionMissUseInspector;

final public class FileFunctionMissUseInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/file-function-missuse.php");
        myFixture.enableInspections(FileFunctionMissUseInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
