package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.ClassConstantCanBeUsedInspector;

public class ClassConstantCanBeUsedInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/lang/classConstant/class-in-the-same-namespace.php");
        myFixture.configureByFile("fixtures/lang/classConstant/class-name-constant.php");
        myFixture.enableInspections(ClassConstantCanBeUsedInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
