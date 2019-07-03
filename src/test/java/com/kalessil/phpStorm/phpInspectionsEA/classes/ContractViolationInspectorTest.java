package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.ContractViolationInspector;

final public class ContractViolationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsPatterns() {
        myFixture.enableInspections(new ContractViolationInspector());
        myFixture.configureByFile("testData/fixtures/classes/contracts-violations.php");
        myFixture.testHighlighting(true, false, true);
    }
}
