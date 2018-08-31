package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.ClassOverridesFieldOfSuperClassInspector;

final public class ClassOverridesFieldOfSuperClassInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        ClassOverridesFieldOfSuperClassInspector inspector = new ClassOverridesFieldOfSuperClassInspector();
        inspector.REPORT_PRIVATE_REDEFINITION              = true;
        myFixture.configureByFile("testData/fixtures/classes/class-overrides-fields.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
}
