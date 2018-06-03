package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.MissingDirectorySeparatorInspector;

final public class MissingDirectorySeparatorInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new MissingDirectorySeparatorInspector());
        myFixture.configureByFile("fixtures/pitfalls/missing-directory-separator.php");
        myFixture.testHighlighting(true, false, true);
    }
}
