package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpDoc.UnknownInspectionInspector;

final public class UnknownInspectionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new UnknownInspectionInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/unknown-inspection-suppression.php");
        myFixture.testHighlighting(true, false, true);
    }
}
