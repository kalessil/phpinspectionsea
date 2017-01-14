package com.kalessil.phpStorm.phpInspectionsEA.phpUnit;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.PhpUnitTestsInspector;

final public class PhpUnitTestsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAssetNullNotNullPatterns() {
        myFixture.configureByFile("fixtures/phpUnit/assert-null-not-null.php");
        myFixture.enableInspections(PhpUnitTestsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsAssetTrueNotTruePatterns() {
        myFixture.configureByFile("fixtures/phpUnit/assert-true-not-true.php");
        myFixture.enableInspections(PhpUnitTestsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsAssetFalseNotFalsePatterns() {
        myFixture.configureByFile("fixtures/phpUnit/assert-false-not-false.php");
        myFixture.enableInspections(PhpUnitTestsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsAssertStringEqualsFilePatterns() {
        myFixture.configureByFile("fixtures/phpUnit/assert-string-equals-file.php");
        myFixture.enableInspections(PhpUnitTestsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
