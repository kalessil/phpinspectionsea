package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.ClassExistenceCheckInspector;

final public class ClassExistenceCheckInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsPatterns() {
        myFixture.enableInspections(new ClassExistenceCheckInspector());
        myFixture.configureByFile("testData/fixtures/classes/class-existence-relation.php");
        myFixture.testHighlighting(true, false, true);
    }
}