package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.MoreThanThreeArgumentsInspector;

final public class MoreThanThreeArgumentsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        MoreThanThreeArgumentsInspector inspector = new MoreThanThreeArgumentsInspector();
        inspector.limit                           = 2;

        myFixture.enableInspections(inspector);

        myFixture.configureByFile("testData/fixtures/codeStyle/too-many-arguments.php");
        myFixture.testHighlighting(true, false, true);
    }
}
