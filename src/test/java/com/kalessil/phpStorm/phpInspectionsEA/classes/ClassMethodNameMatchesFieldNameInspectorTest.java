package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.ClassMethodNameMatchesFieldNameInspector;

final public class ClassMethodNameMatchesFieldNameInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ClassMethodNameMatchesFieldNameInspector());
        myFixture.configureByFile("testData/fixtures/classes/class-field-method-named-identically.php");
        myFixture.testHighlighting(true, false, true);
    }
}
