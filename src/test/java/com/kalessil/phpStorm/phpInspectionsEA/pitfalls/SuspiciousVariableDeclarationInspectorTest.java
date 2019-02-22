package com.kalessil.phpStorm.phpInspectionsEA.pitfalls;


import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.parameters.SuspiciousVariableDeclarationInspector;

final public class SuspiciousVariableDeclarationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SuspiciousVariableDeclarationInspector());
        myFixture.configureByFile("testData/fixtures/pitfalls/suspicious-variable-declaration.php");
        myFixture.testHighlighting(true, false, true);
    }
}