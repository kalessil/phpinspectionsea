package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.DynamicInvocationViaScopeResolutionInspector;

final public class DynamicInvocationViaScopeResolutionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new DynamicInvocationViaScopeResolutionInspector());
        myFixture.configureByFile("testData/fixtures/classes/dynamic-method-invocation-via-scope-resolution.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/classes/dynamic-method-invocation-via-scope-resolution.fixed.php");
    }
}
