package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;

public class SelfClassReferencingInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new SelfClassReferencingInspector());

        myFixture.configureByFile("fixtures/codeStyle/self-class-referencing.php");
        myFixture.testHighlighting(true, false, true);
    }
}
