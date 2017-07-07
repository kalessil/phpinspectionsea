package com.kalessil.phpStorm.phpInspectionsEA.phpUnit;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.PhpUnitTestsInspector;

final public class PhpUnitTestsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsSameNotSamePatterns() {
        PhpUnitTestsInspector inspector     = new PhpUnitTestsInspector();
        inspector.SUGGEST_TO_USE_ASSERTSAME = true;

        myFixture.configureByFile("fixtures/phpUnit/assert-same-not-same.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }

    public void testIfFindsCoversAnnotationPatterns() {
        myFixture.enableInspections(new PhpUnitTestsInspector());

        myFixture.configureByFile("fixtures/phpUnit/covers-annotation.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testIfFindsTestAnnotationPatterns() {
        myFixture.enableInspections(new PhpUnitTestsInspector());

        myFixture.configureByFile("fixtures/phpUnit/test-annotation.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testIfFindsAssetNullNotNullPatterns() {
        myFixture.enableInspections(new PhpUnitTestsInspector());

        myFixture.configureByFile("fixtures/phpUnit/assert-null-not-null.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testIfFindsAssetTrueNotTruePatterns() {
        myFixture.enableInspections(new PhpUnitTestsInspector());

        myFixture.configureByFile("fixtures/phpUnit/assert-true-not-true.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testIfFindsAssetFalseNotFalsePatterns() {
        myFixture.enableInspections(new PhpUnitTestsInspector());

        myFixture.configureByFile("fixtures/phpUnit/assert-false-not-false.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testIfFindsAssertCountPatterns() {
        myFixture.enableInspections(new PhpUnitTestsInspector());

        myFixture.configureByFile("fixtures/phpUnit/assert-count-not-count.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testIfFindsAssertStringEqualsFilePatterns() {
        myFixture.enableInspections(new PhpUnitTestsInspector());

        myFixture.configureByFile("fixtures/phpUnit/assert-string-equals-file.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testIfFindsNormalizationPatterns() {
        myFixture.enableInspections(new PhpUnitTestsInspector());

        myFixture.configureByFile("fixtures/phpUnit/assert-normalization.php");
        myFixture.testHighlighting(true, false, true);
    }
}
