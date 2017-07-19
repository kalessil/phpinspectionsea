package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.ElvisOperatorCanBeUsedInspector;

final public class ElvisOperatorCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/lang/elvis-operator.php");
        myFixture.enableInspections(ElvisOperatorCanBeUsedInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
