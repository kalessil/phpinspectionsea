package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.ClassMemberExistenceCheckInspector;

final public class ClassMemberExistenceCheckInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsPatterns() {
        myFixture.enableInspections(new ClassMemberExistenceCheckInspector());
        myFixture.configureByFile("testData/fixtures/classes/class-member-existence.php");
        myFixture.testHighlighting(true, false, true);
    }
}