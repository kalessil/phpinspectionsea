package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.PackedHashtableOptimizationInspector;

final public class PackedHashtableOptimizationInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        myFixture.enableInspections(new PackedHashtableOptimizationInspector());
        myFixture.configureByFile("testData/fixtures/lang/packed-hashtable-optimization.php");
        myFixture.testHighlighting(true, false, true);
    }
}
