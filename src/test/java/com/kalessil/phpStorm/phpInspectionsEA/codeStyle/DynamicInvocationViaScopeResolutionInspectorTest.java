package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.DynamicInvocationViaScopeResolutionInspector;

final public class DynamicInvocationViaScopeResolutionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/classes/dynamic-method-invocation-via-scope-resolution.php");
        myFixture.enableInspections(DynamicInvocationViaScopeResolutionInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
