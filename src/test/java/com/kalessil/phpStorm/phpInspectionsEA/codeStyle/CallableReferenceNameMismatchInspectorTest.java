package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.CallableReferenceNameMismatchInspector;

final public class CallableReferenceNameMismatchInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CallableReferenceNameMismatchInspector());

        myFixture.configureByFile("fixtures/codeStyle/callable-name-case-mismatch.php");
        myFixture.testHighlighting(true, false, true);
    }
}