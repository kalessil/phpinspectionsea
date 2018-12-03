package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TraitsMethodsConflictsInspector;

final public class TraitsMethodsConflictsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new TraitsMethodsConflictsInspector());
        myFixture.configureByFile("testData/fixtures/classes/traits-methods-conflicts.php");
        myFixture.testHighlighting(true, false, true);
    }
}
