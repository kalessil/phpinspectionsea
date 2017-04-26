package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.UselessReturnInspector;

final public class UselessReturnInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(UselessReturnInspector.class);

        myFixture.configureByFile("fixtures/codeStyle/useless-returns.php");
        myFixture.testHighlighting(true, false, true);
    }
}