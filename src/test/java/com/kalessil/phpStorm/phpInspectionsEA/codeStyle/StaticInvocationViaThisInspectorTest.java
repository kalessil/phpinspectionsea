package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.StaticInvocationViaThisInspector;

final public class StaticInvocationViaThisInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        StaticInvocationViaThisInspector inspector = new StaticInvocationViaThisInspector();
        inspector.RESPECT_PHPUNIT_STANDARDS        = true;

        myFixture.configureByFile("fixtures/classes/static-method-invocation-via-this.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
}
