package com.kalessil.phpStorm.phpInspectionsEA.phpUnit;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.PhpUnitDeprecationsInspector;

final public class PhpUnitDeprecationsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsPatterns() {
        myFixture.enableInspections(new PhpUnitDeprecationsInspector());
        myFixture.configureByFile("testData/fixtures/phpUnit/deprecations.php");
        myFixture.testHighlighting(true, false, true);
    }
}
