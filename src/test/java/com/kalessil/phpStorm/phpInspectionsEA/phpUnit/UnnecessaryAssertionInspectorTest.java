package com.kalessil.phpStorm.phpInspectionsEA.phpUnit;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.UnnecessaryAssertionInspector;

final public class UnnecessaryAssertionInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsCoversAnnotationPatterns() {
        myFixture.enableInspections(new UnnecessaryAssertionInspector());
        myFixture.configureByFile("fixtures/phpUnit/unnecessary-assertion.php");
        myFixture.testHighlighting(true, false, true);
    }
}
