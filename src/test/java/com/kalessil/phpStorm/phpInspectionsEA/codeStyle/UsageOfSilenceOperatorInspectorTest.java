package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.UsageOfSilenceOperatorInspector;

final public class UsageOfSilenceOperatorInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        UsageOfSilenceOperatorInspector inspector = new UsageOfSilenceOperatorInspector();
        inspector.optionRespectContext = true;

        myFixture.configureByFile("fixtures/codeStyle/usage-of-silence-operator.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
}
