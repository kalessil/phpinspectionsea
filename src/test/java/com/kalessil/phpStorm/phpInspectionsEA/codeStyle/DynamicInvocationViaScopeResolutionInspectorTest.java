package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.DynamicInvocationViaScopeResolutionInspector;

final public class DynamicInvocationViaScopeResolutionInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/classes/dynamic-method-incocation-via-scope-resolution.php");
        myFixture.enableInspections(DynamicInvocationViaScopeResolutionInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
