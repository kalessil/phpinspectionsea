package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TraitsPropertiesConflictsInspector;

final public class TraitsPropertiesConflictsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/classes/traits-properties-conflicts.php");
        myFixture.enableInspections(TraitsPropertiesConflictsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
