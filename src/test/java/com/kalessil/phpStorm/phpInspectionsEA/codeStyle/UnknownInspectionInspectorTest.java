package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.lang.inspections.probablyBug.missingParentCall.PhpMissingParentCallCommonInspection;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpDoc.UnknownInspectionInspector;

final public class UnknownInspectionInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/unknown-inspection-suppression.php");
        myFixture.enableInspections(UnknownInspectionInspector.class, PhpMissingParentCallCommonInspection.class);
        myFixture.testHighlighting(true, false, true);
    }
}
