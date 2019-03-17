package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.nullCoalescing.IssetArgumentExistenceInspector;

final public class IssetArgumentExistenceInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);
        final IssetArgumentExistenceInspector inspector = new IssetArgumentExistenceInspector();
        inspector.IGNORE_INCLUDES                       = false;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/controlFlow/isset-operations-variable-existence.php");
        myFixture.testHighlighting(true, false, true);
    }
}
