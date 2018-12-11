package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.MissUsingParentKeywordInspector;

final public class MissUsingParentKeywordInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsMissingStatements() {
        myFixture.enableInspections(new MissUsingParentKeywordInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/missusing-parent-keyword.php");
        myFixture.testHighlighting(true, false, true);
    }
}
