package com.kalessil.phpStorm.phpInspectionsEA.security;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.security.CommandExecutionAsSuperUserInspector;

final public class CommandExecutionAsSuperUserInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new CommandExecutionAsSuperUserInspector());
        myFixture.configureByFile("fixtures/security/command-execution-as-superuser.php");
        myFixture.testHighlighting(true, false, true);
    }
}
