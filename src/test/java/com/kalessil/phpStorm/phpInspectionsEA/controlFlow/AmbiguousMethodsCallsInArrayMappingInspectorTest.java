package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.AmbiguousMethodsCallsInArrayMappingInspector;

final public class AmbiguousMethodsCallsInArrayMappingInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new AmbiguousMethodsCallsInArrayMappingInspector());
        myFixture.configureByFile("testData/fixtures/controlFlow/array-mapping-ambiguous-calls.php");
        myFixture.testHighlighting(true, false, true);
    }
}
