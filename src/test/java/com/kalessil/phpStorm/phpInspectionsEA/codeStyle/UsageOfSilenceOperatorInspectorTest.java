package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.UsageOfSilenceOperatorInspector;

final public class UsageOfSilenceOperatorInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        UsageOfSilenceOperatorInspector inspector = new UsageOfSilenceOperatorInspector();
        inspector.RESPECT_CONTEXT                 = true;

        myFixture.configureByFile("fixtures/codeStyle/usage-of-silence-operator.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
}
