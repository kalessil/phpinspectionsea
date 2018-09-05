package com.kalessil.phpStorm.phpInspectionsEA.deadCode;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.SenselessProxyMethodInspector;

final public class SenselessProxyMethodInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsCommonPatterns() {
        myFixture.enableInspections(new SenselessProxyMethodInspector());
        myFixture.configureByFile("testData/fixtures/deadCode/senselessProxy/senseless-proxy-common.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsSignaturePatterns() {
        myFixture.enableInspections(new SenselessProxyMethodInspector());
        myFixture.configureByFile("testData/fixtures/deadCode/senselessProxy/senseless-proxy-signature.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsReturnPatterns() {
        myFixture.enableInspections(new SenselessProxyMethodInspector());
        myFixture.configureByFile("testData/fixtures/deadCode/senselessProxy/senseless-proxy-return.php");
        myFixture.testHighlighting(true, false, true);
    }
}
