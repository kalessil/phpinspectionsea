package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.jetbrains.php.lang.inspections.probablyBug.missingParentCall.PhpMissingParentCallCommonInspection;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpDoc.UnknownInspectionInspector;

final public class UnknownInspectionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/unknown-inspection-suppression.php");
        myFixture.enableInspections(UnknownInspectionInspector.class, PhpMissingParentCallCommonInspection.class);
        myFixture.testHighlighting(true, false, true);
    }
}
