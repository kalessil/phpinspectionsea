package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.strictInterfaces.ArrayTypeOfParameterByDefaultValueInspector;

final public class ArrayTypeOfParameterByDefaultValueInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new ArrayTypeOfParameterByDefaultValueInspector());
        myFixture.configureByFile("testData/fixtures/types/type-can-be-array.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/types/type-can-be-array.fixed.php");
    }
}
