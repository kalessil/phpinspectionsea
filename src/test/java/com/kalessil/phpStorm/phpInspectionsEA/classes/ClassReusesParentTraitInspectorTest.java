package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.ClassReusesParentTraitInspector;

final public class ClassReusesParentTraitInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ClassReusesParentTraitInspector());
        myFixture.configureByFile("testData/fixtures/classes/re-uses-parent-trait.php");
        myFixture.testHighlighting(true, false, true);
    }
}
