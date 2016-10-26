package com.kalessil.phpStorm.phpInspectionsEA.tests.designPatterns;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.PrivateConstructorSemanticsInspector;
import com.kalessil.phpStorm.phpInspectionsEA.tests.utils.FixturesLocationUtil;

public class PrivateConstructorSemanticsInspectorTest extends CodeInsightFixtureTestCase {
    @Override
    protected String getBasePath() {
        return FileUtil.toSystemDependentName(FixturesLocationUtil.RELATIVE_TEST_DATA_PATH);
    }

    public void testValidEntityWithOwnFactoryMethodCreate () {
        myFixture.configureByFile("fixtures/designPatterns/entiry-with-own-factory-methods-create.php");
        myFixture.enableInspections(PrivateConstructorSemanticsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testValidEntityWithOwnFactoryMethodFrom () {
        myFixture.configureByFile("fixtures/designPatterns/entiry-with-own-factory-methods-from.php");
        myFixture.enableInspections(PrivateConstructorSemanticsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testMissingFinal () {
        myFixture.configureByFile("fixtures/designPatterns/util-class-needs-final.php");
        myFixture.enableInspections(PrivateConstructorSemanticsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testMissingUtilNameEnding () {
        myFixture.configureByFile("fixtures/designPatterns/util-class-needs-correct-naming.php");
        myFixture.enableInspections(PrivateConstructorSemanticsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

    public void testIncorrectSingletonConstructorVisibility () {
        myFixture.configureByFile("fixtures/designPatterns/singleton-constructor-needs-be-protected.php");
        myFixture.enableInspections(PrivateConstructorSemanticsInspector.class);
        myFixture.testHighlighting(true, false, true);
    }

}
