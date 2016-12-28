package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.CallableReferenceNameMismatchInspector;

final public class CallableReferenceNameMismatchInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/callable-name-case-mismatch.php");
        myFixture.enableInspections(CallableReferenceNameMismatchInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}