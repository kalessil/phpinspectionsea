package com.kalessil.phpStorm.phpInspectionsEA.semanticalAnalysis;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.DuplicateArrayKeysInspector;

public class DuplicateArrayKeysInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new DuplicateArrayKeysInspector());
        myFixture.configureByFile("testData/fixtures/semanticalAnalysis/duplicate-array-keys.php");
        myFixture.testHighlighting(true, false, true);
    }
}
