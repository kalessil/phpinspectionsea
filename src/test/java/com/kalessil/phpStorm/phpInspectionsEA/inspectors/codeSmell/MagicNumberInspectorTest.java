package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

public class MagicNumberInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new MagicNumberInspector());

        myFixture.configureByFile("fixtures/codeSmell/MagicNumber.php");
        myFixture.testHighlighting(true, false, true);
    }
}
