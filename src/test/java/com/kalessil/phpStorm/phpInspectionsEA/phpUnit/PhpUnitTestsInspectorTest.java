package com.kalessil.phpStorm.phpInspectionsEA.phpUnit;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.PhpUnitTestsInspector;

final public class PhpUnitTestsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/phpUnit/assert-null-not-null.php");
        myFixture.enableInspections(PhpUnitTestsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
