package com.kalessil.phpStorm.phpInspectionsEA.phpUnit;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.PhpUnitDeprecationsInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.PhpUnitVersion;

final public class PhpUnitDeprecationsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsPatterns80() {
        final PhpUnitDeprecationsInspector inspector = new PhpUnitDeprecationsInspector();
        inspector.PHP_UNIT_VERSION                   = PhpUnitVersion.PHPUNIT80;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/phpUnit/deprecations.phpunit80.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsPatterns91() {
        final PhpUnitDeprecationsInspector inspector = new PhpUnitDeprecationsInspector();
        inspector.PHP_UNIT_VERSION                   = PhpUnitVersion.PHPUNIT91;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/phpUnit/deprecations.phpunit91.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/phpUnit/deprecations.phpunit91.fixed.php");
    }
}
