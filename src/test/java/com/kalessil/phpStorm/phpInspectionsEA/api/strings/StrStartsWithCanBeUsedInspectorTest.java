package com.kalessil.phpStorm.phpInspectionsEA.api.strings;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.StrStartsWithCanBeUsedInspector;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;

final public class StrStartsWithCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase
{
    public void testIfFindsAllPatterns() {
        PhpLanguageLevel.set(PhpLanguageLevel.PHP800);
        myFixture.enableInspections(new StrStartsWithCanBeUsedInspector());
        myFixture.configureByFile("testData/fixtures/api/strings/str_starts_with.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/strings/str_starts_with.fixed.php");
        PhpLanguageLevel.set(null);
    }
}