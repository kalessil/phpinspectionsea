package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.StaticInvocationViaThisInspector;

final public class UnknownInspectionInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/unknown-inspection-suppression.php");
        myFixture.enableInspections(StaticInvocationViaThisInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
