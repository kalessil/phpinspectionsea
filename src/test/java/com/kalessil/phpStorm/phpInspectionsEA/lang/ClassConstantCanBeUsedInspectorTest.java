package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.codeInsight.intention.IntentionAction;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.ClassConstantCanBeUsedInspector;

final public class ClassConstantCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        ClassConstantCanBeUsedInspector inspector = new ClassConstantCanBeUsedInspector();
        inspector.optionImportClassesOnQF = true;
        inspector.optionUseRelativeQF = true;
        inspector.optionLookRootNsUp = true;

        myFixture.configureByFile("fixtures/lang/classConstant/class-in-the-same-namespace.php");
        myFixture.configureByFile("fixtures/lang/classConstant/class-name-constant-ns.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);

        for (IntentionAction fix : myFixture.getAllQuickFixes()) {
            myFixture.launchAction(fix);
        }
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile(
            "fixtures/lang/classConstant/class-name-constant-ns.php",
            "fixtures/lang/classConstant/class-name-constant-ns.fixed.php",
            false
        );
    }
}
