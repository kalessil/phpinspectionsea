package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.SenselessCommaInArrayDefinitionInspector;

final public class SenselessCommaInArrayDefinitionInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/last-comma-in-array.php");
        myFixture.enableInspections(SenselessCommaInArrayDefinitionInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

