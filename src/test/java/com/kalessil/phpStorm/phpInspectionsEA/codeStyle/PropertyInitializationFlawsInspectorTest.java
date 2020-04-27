package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.openapi.application.ApplicationInfo;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.PropertyInitializationFlawsInspector;

final public class PropertyInitializationFlawsInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testNullInitPatterns() {
        final PropertyInitializationFlawsInspector inspector = new PropertyInitializationFlawsInspector();
        inspector.REPORT_INIT_FLAWS                          = true;
        inspector.REPORT_DEFAULTS_FLAWS                      = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/codeStyle/property-null-initialization.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/codeStyle/property-null-initialization.fixed.php");
    }
    public void testPropertyOverridePatterns() {
        final PropertyInitializationFlawsInspector inspector = new PropertyInitializationFlawsInspector();
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/codeStyle/property-initialization-override.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testClassReferenceResolvingPatterns() {
        final PropertyInitializationFlawsInspector inspector = new PropertyInitializationFlawsInspector();
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/codeStyle/property-initialization-class-references.php");
        myFixture.testHighlighting(true, false, true);
    }
    public void testTypedPropertiesPatterns() {
        final PhpLanguageLevel level = PhpLanguageLevel.parse("7.4");
        if (level != null && level.getVersionString().equals("7.4")) {
            /* In 2019.1 environment, typed properties are not identifying properly */
            final ApplicationInfo about = ApplicationInfo.getInstance();
            final boolean executeTest   = ! (about.getMajorVersion().equals("2019") && about.getMinorVersion().startsWith("1"));
            if (executeTest) {
                PhpProjectConfigurationFacade.getInstance(myFixture.getProject()).setLanguageLevel(level);
                myFixture.enableInspections(new PropertyInitializationFlawsInspector());
                myFixture.configureByFile("testData/fixtures/codeStyle/property-initialization-typed-properties.php");
                myFixture.testHighlighting(true, false, true);
            }
        }
    }
}
