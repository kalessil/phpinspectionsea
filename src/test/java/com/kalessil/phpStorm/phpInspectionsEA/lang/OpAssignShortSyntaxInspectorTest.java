package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.OpAssignShortSyntaxInspector;

final public class OpAssignShortSyntaxInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/lang/op-assign-short-syntax.php");
        myFixture.enableInspections(OpAssignShortSyntaxInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}