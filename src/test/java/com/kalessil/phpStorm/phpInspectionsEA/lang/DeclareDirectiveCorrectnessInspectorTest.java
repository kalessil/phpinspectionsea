package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.DeclareDirectiveCorrectnessInspector;

final public class DeclareDirectiveCorrectnessInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new DeclareDirectiveCorrectnessInspector());

        myFixture.configureByFile("fixtures/lang/declare-directive.php");
        myFixture.testHighlighting(true, false, true);
    }
}

