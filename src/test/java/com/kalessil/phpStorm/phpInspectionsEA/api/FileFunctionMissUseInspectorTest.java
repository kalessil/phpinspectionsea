package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem.FileFunctionMissUseInspector;

final public class FileFunctionMissUseInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new FileFunctionMissUseInspector());
        myFixture.configureByFile("fixtures/api/file-function-misuse.php");
        myFixture.testHighlighting(true, false, true);
    }
}
