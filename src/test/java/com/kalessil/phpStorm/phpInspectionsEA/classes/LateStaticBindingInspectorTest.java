package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.LateStaticBindingInspector;

final public class LateStaticBindingInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new LateStaticBindingInspector());
        myFixture.configureByFile("testData/fixtures/classes/late-static-binding.php");
        myFixture.testHighlighting(true, false, true);
    }
}
