package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.ClassReImplementsParentInterfaceInspector;

final public class ClassReImplementsParentInterfaceInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/classes/re-implements-parent-interface.php");
        myFixture.enableInspections(ClassReImplementsParentInterfaceInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}

