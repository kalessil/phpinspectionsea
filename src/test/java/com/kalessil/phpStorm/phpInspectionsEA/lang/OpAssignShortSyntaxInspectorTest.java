package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.OpAssignShortSyntaxInspector;

final public class OpAssignShortSyntaxInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/lang/op-assign-short-syntax.php");
        myFixture.enableInspections(OpAssignShortSyntaxInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}