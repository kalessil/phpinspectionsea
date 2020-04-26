package com.kalessil.phpStorm.phpInspectionsEA.phpUnit;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.PhpUnitDeprecationsInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.PhpUnitVersion;

final public class PhpUnitDeprecationsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsPatterns() {
        final PhpUnitDeprecationsInspector inspector = new PhpUnitDeprecationsInspector();
        inspector.PHP_UNIT_VERSION                   = PhpUnitVersion.PHPUNIT80;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/phpUnit/deprecations.php");
        myFixture.testHighlighting(true, false, true);
    }
}
