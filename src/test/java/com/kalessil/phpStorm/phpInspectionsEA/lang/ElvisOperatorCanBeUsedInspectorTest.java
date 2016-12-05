package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.ElvisOperatorCanBeUsedInspector;

public class ElvisOperatorCanBeUsedInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/lang/elvis-operator.php");
        myFixture.enableInspections(ElvisOperatorCanBeUsedInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
