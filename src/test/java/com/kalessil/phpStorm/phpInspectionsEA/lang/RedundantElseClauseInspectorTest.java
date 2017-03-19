package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.RedundantElseClauseInspector;

final public class RedundantElseClauseInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/unnecessary-else-elseif.php");
        myFixture.enableInspections(RedundantElseClauseInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}