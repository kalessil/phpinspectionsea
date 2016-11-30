package com.kalessil.phpStorm.phpInspectionsEA.deadCode;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SenselessProxyMethodInspector;

final public class SenselessProxyMethodInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/deadCode/senseless-proxy.php");
        myFixture.enableInspections(SenselessProxyMethodInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}