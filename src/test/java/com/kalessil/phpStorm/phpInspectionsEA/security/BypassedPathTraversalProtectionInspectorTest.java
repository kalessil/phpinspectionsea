package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.BypassedPathTraversalProtectionInspector;

final public class BypassedPathTraversalProtectionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new BypassedPathTraversalProtectionInspector());
        myFixture.configureByFile("fixtures/security/bypassed-path-traverse-protection.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("fixtures/security/bypassed-path-traverse-protection.fixed.php");
    }
}
