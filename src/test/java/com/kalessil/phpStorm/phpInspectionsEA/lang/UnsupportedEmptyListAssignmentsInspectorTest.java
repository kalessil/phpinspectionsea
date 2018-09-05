package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.UnsupportedEmptyListAssignmentsInspector;

final public class UnsupportedEmptyListAssignmentsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP700);
        myFixture.enableInspections(new UnsupportedEmptyListAssignmentsInspector());
        myFixture.configureByFile("testData/fixtures/lang/unsupported-empty-list-assignments.php");
        myFixture.testHighlighting(true, false, true);
    }
}
