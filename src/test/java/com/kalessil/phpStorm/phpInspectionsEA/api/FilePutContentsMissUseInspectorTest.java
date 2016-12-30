package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem.FilePutContentsMissUseInspector;

final public class FilePutContentsMissUseInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/api/file_put_contents-missuse.php");
        myFixture.enableInspections(FilePutContentsMissUseInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}