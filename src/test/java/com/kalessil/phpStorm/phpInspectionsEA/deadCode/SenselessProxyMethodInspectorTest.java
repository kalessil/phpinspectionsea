package com.kalessil.phpStorm.phpInspectionsEA.deadCode;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.SenselessProxyMethodInspector;

final public class SenselessProxyMethodInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsCommonPatterns() {
        myFixture.configureByFile("fixtures/deadCode/senseless-proxy-common.php");
        myFixture.enableInspections(SenselessProxyMethodInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsSignaturePatterns() {
        myFixture.configureByFile("fixtures/deadCode/senseless-proxy-signature.php");
        myFixture.enableInspections(SenselessProxyMethodInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}