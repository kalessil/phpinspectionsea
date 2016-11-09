package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.PropertyCanBeStaticInspector;

final public class PropertyCanBeStaticInspectorTest  extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/classes/property-can-be-static.php");
        myFixture.enableInspections(PropertyCanBeStaticInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testFalsePositives() {
        myFixture.configureByFile("fixtures/classes/property-can-be-static-false-positives.php");
        myFixture.enableInspections(PropertyCanBeStaticInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}