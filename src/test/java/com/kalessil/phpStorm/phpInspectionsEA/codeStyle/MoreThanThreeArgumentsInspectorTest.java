package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.MoreThanThreeArgumentsInspector;

final public class MoreThanThreeArgumentsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        MoreThanThreeArgumentsInspector inspector = new MoreThanThreeArgumentsInspector();
        inspector.limit                           = 2;

        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/codeStyle/too-many-arguments.php");
        myFixture.testHighlighting(true, false, true);
    }
}
