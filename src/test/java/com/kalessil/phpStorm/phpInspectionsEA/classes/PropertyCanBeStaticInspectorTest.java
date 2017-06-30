package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.PropertyCanBeStaticInspector;

final public class PropertyCanBeStaticInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new PropertyCanBeStaticInspector());

        myFixture.configureByFile("fixtures/classes/property-can-be-static.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.enableInspections(new PropertyCanBeStaticInspector());

        myFixture.configureByFile("fixtures/classes/property-can-be-static-false-positives.php");
        myFixture.testHighlighting(true, false, true);
    }
}