package com.kalessil.phpStorm.phpInspectionsEA.api;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.fileSystem.CascadingDirnameCallsInspector;

final public class CascadingDirnameCallsInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(PhpLanguageLevel.PHP710);

        myFixture.configureByFile("fixtures/api/cascade-dirname.php");
        myFixture.enableInspections(CascadingDirnameCallsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}