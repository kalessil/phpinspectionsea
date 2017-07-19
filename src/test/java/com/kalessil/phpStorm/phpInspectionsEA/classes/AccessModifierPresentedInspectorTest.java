package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.AccessModifierPresentedInspector;

final public class AccessModifierPresentedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        AccessModifierPresentedInspector inspector = new AccessModifierPresentedInspector();
        inspector.ANALYZE_INTERFACES               = true;

        myFixture.configureByFile("fixtures/classes/access-modifiers.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
}