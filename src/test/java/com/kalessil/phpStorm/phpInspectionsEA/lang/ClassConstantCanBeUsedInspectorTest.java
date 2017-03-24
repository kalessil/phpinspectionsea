package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.ClassConstantCanBeUsedInspector;

import java.util.List;

final public class ClassConstantCanBeUsedInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/lang/classConstant/class-in-the-same-namespace.php");
        myFixture.configureByFile("fixtures/lang/classConstant/class-name-constant-ns.php");
        myFixture.enableInspections(ClassConstantCanBeUsedInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testQuickFixOutput() {
        myFixture.configureByFile("fixtures/lang/classConstant/class-in-the-same-namespace.php");
        myFixture.configureByFile("fixtures/lang/classConstant/class-name-constant-ns.php");
        myFixture.enableInspections(ClassConstantCanBeUsedInspector.class);
        myFixture.testHighlighting(true, false, false);

        List<IntentionAction> quickFixes = myFixture.getAllQuickFixes();
        for (IntentionAction fix: quickFixes) {
            myFixture.launchAction(fix);
        }

        myFixture.setTestDataPath("./");
        myFixture.checkResultByFile("fixtures/lang/classConstant/class-name-constant-ns.fixed.php");
    }
}
