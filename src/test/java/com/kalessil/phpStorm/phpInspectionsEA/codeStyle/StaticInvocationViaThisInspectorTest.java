package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.StaticInvocationViaThisInspector;

final public class StaticInvocationViaThisInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final StaticInvocationViaThisInspector inspector = new StaticInvocationViaThisInspector();
        inspector.RESPECT_PHPUNIT_STANDARDS              = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("fixtures/classes/static-method-invocation-via-this.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/classes/static-method-invocation-via-this.fixed.php");
    }
}
