package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.AmbiguousMemberInitializationInspector;

public class AmbiguousMemberInitializationInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/codeStyle/property-null-initialization.php");
        myFixture.enableInspections(AmbiguousMemberInitializationInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}