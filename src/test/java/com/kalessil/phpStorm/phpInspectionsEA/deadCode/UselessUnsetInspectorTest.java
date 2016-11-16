package com.kalessil.phpStorm.phpInspectionsEA.deadCode;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.UselessUnsetInspector;

public class UselessUnsetInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/deadCode/useless-unset.php");
        myFixture.enableInspections(UselessUnsetInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}