package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.OverridingDeprecatedMethodInspector;

final public class OverridingDeprecatedMethodInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsInconsistentGetsSetsPatterns() {
        myFixture.configureByFile("fixtures/classes/overriding-deprecated-methods.php");
        myFixture.enableInspections(OverridingDeprecatedMethodInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
