package com.kalessil.phpStorm.phpInspectionsEA.api.arrays;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.arrays.ArrayIsListCanBeUsedInspector;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;

final public class ArrayIsListCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpLanguageLevel.set(PhpLanguageLevel.PHP810);
        myFixture.enableInspections(new ArrayIsListCanBeUsedInspector());
        myFixture.configureByFile("testData/fixtures/api/array/array_is_list.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/array/array_is_list.fixed.php");
        PhpLanguageLevel.set(null);
    }
}
