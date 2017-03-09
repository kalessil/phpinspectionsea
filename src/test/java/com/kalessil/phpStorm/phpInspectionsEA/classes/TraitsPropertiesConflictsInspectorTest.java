package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.TraitsPropertiesConflictsInspector;

final public class TraitsPropertiesConflictsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/classes/traits-properties-conflicts.php");
        myFixture.enableInspections(TraitsPropertiesConflictsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
