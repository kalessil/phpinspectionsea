package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem.FileFunctionMissUseInspector;

final public class FileFunctionMissUseInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/file-function-misuse.php");
        myFixture.enableInspections(FileFunctionMissUseInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
