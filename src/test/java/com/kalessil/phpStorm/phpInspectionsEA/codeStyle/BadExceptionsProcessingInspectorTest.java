package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.BadExceptionsProcessingInspector;

final public class BadExceptionsProcessingInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new BadExceptionsProcessingInspector());

        myFixture.configureByFile("fixtures/codeStyle/bad-exceptions-handling.php");
        myFixture.testHighlighting(true, false, true);
    }
}
