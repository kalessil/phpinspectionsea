package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.UsageOfSilenceOperatorInspector;


public class UsageOfSilenceOperatorInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/usage-of-silence-operator.php");
        myFixture.enableInspections(UsageOfSilenceOperatorInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
