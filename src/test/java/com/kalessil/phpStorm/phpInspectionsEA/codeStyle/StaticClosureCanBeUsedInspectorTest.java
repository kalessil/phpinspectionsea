package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.StaticClosureCanBeUsedInspector;

import java.util.Arrays;

final public class StaticClosureCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new StaticClosureCanBeUsedInspector());
        myFixture.configureByFile("testData/fixtures/codeStyle/static-closure-use.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/codeStyle/static-closure-use.fixed.php");
    }
    public void testIfFindsArrowFunctionPatterns() {
        final PhpLanguageLevel level = PhpLanguageLevel.parse("7.4");
        if (level != null && level.getVersionString().equals("7.4")) {
            /* looks like 7.4 feature were introduced in multiple batches */
            final boolean hasArrowFunctions = Arrays.stream(PhpLanguageFeature.class.getEnumConstants())
                    .anyMatch(f -> f.toString().contains("Arrow function"));
            if (hasArrowFunctions) {
                PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(level);
                myFixture.enableInspections(new StaticClosureCanBeUsedInspector());
                myFixture.configureByFile("testData/fixtures/codeStyle/static-closure-use.php74.php");
                myFixture.testHighlighting(true, false, true);
            }
        }
    }
}
