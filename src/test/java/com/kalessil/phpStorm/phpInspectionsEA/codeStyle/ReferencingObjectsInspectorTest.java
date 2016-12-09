package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.ReferencingObjectsInspector;

final public class ReferencingObjectsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/referencing-objects.php");
        myFixture.enableInspections(ReferencingObjectsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
