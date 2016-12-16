package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.AmbiguousMethodsCallsInArrayMappingInspector;

final public class AmbiguousMethodsCallsInArrayMappingInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/array-mapping-ambiguous-calls.php");
        myFixture.enableInspections(AmbiguousMethodsCallsInArrayMappingInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
