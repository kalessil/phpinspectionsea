package com.kalessil.phpStorm.phpInspectionsEA.classes;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes.DeprecatedConstructorStyleInspector;

final public class DeprecatedConstructorStyleInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new DeprecatedConstructorStyleInspector());
        myFixture.configureByFile("testData/fixtures/classes/deprecated-constructors.php");
        myFixture.testHighlighting(true, false, true);
    }
}
