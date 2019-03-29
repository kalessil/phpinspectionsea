package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.BadExceptionsProcessingInspector;

final public class BadExceptionsProcessingInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new BadExceptionsProcessingInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/bad-exceptions-handling.php");
        myFixture.testHighlighting(true, false, true);
    }
}
