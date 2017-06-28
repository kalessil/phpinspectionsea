package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.SenselessCommaInArrayDefinitionInspector;

public final class SenselessCommaInArrayDefinitionInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SenselessCommaInArrayDefinitionInspector());
        myFixture.configureByFile("fixtures/codeStyle/last-comma-in-array.php");
        myFixture.testHighlighting(true, false, true);
    }
}

