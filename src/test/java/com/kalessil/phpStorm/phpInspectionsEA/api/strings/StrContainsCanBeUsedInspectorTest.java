package com.kalessil.phpStorm.phpInspectionsEA.api.strings;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.StrContainsCanBeUsedInspector;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;

final public class StrContainsCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase
{
    public void testIfFindsLanguageLevelPatterns() {
        PhpLanguageLevel.set(PhpLanguageLevel.PHP800);
        myFixture.enableInspections(new StrContainsCanBeUsedInspector());
        myFixture.configureByFile("testData/fixtures/api/strings/str_contains.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/strings/str_contains.fixed.php");
        PhpLanguageLevel.set(null);
    }

    public void testIfFindsPolyfillPatterns() {
        myFixture.enableInspections(new StrContainsCanBeUsedInspector());
        myFixture.configureByFile("testData/fixtures/api/strings/str_contains.polyfill.php");
        myFixture.configureByFile("testData/fixtures/api/strings/str_contains.php");
        myFixture.testHighlighting(true, false, true);
    }
}
