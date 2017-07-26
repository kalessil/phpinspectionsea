package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.ReferencingObjectsInspector;

final public class ReferencingObjectsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/referencing-objects.php");
        myFixture.enableInspections(ReferencingObjectsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
