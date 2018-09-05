package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.RedundantElseClauseInspector;

final public class RedundantElseClauseInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new RedundantElseClauseInspector());
        myFixture.configureByFile("testData/fixtures/controlFlow/unnecessary-else-elseif.php");
        myFixture.testHighlighting(true, false, true);
    }
}
