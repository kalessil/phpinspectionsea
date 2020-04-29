package com.kalessil.phpStorm.phpInspectionsEA.api.strings;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strings.StrEndsWithCanBeUsedInspector;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;

public class StrEndsWithCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase
{
    public void testIfFindsAllPatterns() {
        PhpLanguageLevel.set(PhpLanguageLevel.PHP800);
        myFixture.enableInspections(new StrEndsWithCanBeUsedInspector());
        myFixture.configureByFile("testData/fixtures/api/strings/str_ends_with.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/api/strings/str_ends_with.fixed.php");
        PhpLanguageLevel.set(null);
    }
}