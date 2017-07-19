package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.exceptions.ExceptionsAnnotatingAndHandlingInspector;

final public class ExceptionsAnnotatingAndHandlingInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testAnnotationsProcessing() {
        myFixture.enableInspections(new ExceptionsAnnotatingAndHandlingInspector());

        myFixture.configureByFile("fixtures/controlFlow/exceptions-handling-annotations-processing.php");
        myFixture.testHighlighting(true, false, true);
    }
}
