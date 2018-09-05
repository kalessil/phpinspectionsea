package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.OverridingDeprecatedMethodInspector;

final public class OverridingDeprecatedMethodInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsInconsistentGetsSetsPatterns() {
        myFixture.enableInspections(new OverridingDeprecatedMethodInspector());
        myFixture.configureByFile("testData/fixtures/classes/overriding-deprecated-constructs.php");
        myFixture.testHighlighting(true, false, true);
    }
}
