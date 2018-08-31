package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UnsupportedStringOffsetOperationsInspector;

final public class UnsupportedStringOffsetOperationsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        myFixture.enableInspections(new UnsupportedStringOffsetOperationsInspector());
        myFixture.configureByFile("testData/fixtures/lang/unsupported-string-offset.php");
        myFixture.testHighlighting(true, false, true);
    }
}
