package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.DeclareDirectiveCorrectnessInspector;

final public class DeclareDirectiveCorrectnessInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new DeclareDirectiveCorrectnessInspector());
        myFixture.configureByFile("testData/fixtures/lang/declare-directive.php");
        myFixture.testHighlighting(true, false, true);
    }
}

