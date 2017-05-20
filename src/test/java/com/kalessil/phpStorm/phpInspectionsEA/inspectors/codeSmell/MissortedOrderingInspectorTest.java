package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

public class MissortedOrderingInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(MissortedOrderingInspector.class);
        myFixture.configureByFile("fixtures/inspectors/codeSmell/MissortedOrdering.php");
        myFixture.testHighlighting(true, false, true);
    }
}
