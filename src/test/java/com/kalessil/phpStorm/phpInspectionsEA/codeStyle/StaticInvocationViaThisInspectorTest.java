package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.StaticInvocationViaThisInspector;

final public class StaticInvocationViaThisInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/classes/static-method-invocation-via-this.php");
        myFixture.enableInspections(StaticInvocationViaThisInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
