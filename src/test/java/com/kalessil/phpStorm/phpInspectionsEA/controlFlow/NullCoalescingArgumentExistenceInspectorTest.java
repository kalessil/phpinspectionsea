package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.NullCoalescingArgumentExistenceInspector;

final public class NullCoalescingArgumentExistenceInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(NullCoalescingArgumentExistenceInspector.class);

        myFixture.configureByFile("fixtures/controlFlow/null-coalescing-variable-existence.php");
        myFixture.testHighlighting(true, false, true);
    }
}