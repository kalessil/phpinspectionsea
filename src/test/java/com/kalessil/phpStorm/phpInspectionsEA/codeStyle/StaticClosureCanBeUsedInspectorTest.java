package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.StaticClosureCanBeUsedInspector;

final public class StaticClosureCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new StaticClosureCanBeUsedInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/static-closure-use.php");
        myFixture.testHighlighting(true, false, true);
    }
}
