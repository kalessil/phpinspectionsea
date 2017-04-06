package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.OffsetOperationsInspector;

final public class OffsetOperationsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/types/offset-operations.php");
        myFixture.enableInspections(OffsetOperationsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}