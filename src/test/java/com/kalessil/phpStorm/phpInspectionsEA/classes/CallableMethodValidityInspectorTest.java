package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.CallableMethodValidityInspector;

final public class CallableMethodValidityInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsPatterns() {
        myFixture.enableInspections(CallableMethodValidityInspector.class);

        myFixture.configureByFile("fixtures/classes/callable-methods-validity.php");
        myFixture.testHighlighting(true, false, true);
    }
}
