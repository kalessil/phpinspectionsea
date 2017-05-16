package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.CallableReferenceNameMismatchInspector;

final public class CallableReferenceNameMismatchInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CallableReferenceNameMismatchInspector());

        myFixture.configureByFile("fixtures/codeStyle/callable-name-case-mismatch.php");
        myFixture.testHighlighting(true, false, true);
    }
}