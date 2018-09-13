package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.ClassConstantCanBeUsedInspector;

final public class ClassConstantCanBeUsedInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        final ClassConstantCanBeUsedInspector inspector = new ClassConstantCanBeUsedInspector();
        inspector.IMPORT_CLASSES_ON_QF                  = true;
        inspector.USE_RELATIVE_QF                       = true;
        inspector.LOOK_ROOT_NS_UP                       = true;
        myFixture.configureByFile("testData/fixtures/lang/classConstant/class-in-the-same-namespace.php");
        myFixture.configureByFile("testData/fixtures/lang/classConstant/class-name-constant-ns.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile(
            "testData/fixtures/lang/classConstant/class-name-constant-ns.php",
            "testData/fixtures/lang/classConstant/class-name-constant-ns.fixed.php",
            false
        );
    }
    public void testConflictingClassNames() {
        final ClassConstantCanBeUsedInspector inspector = new ClassConstantCanBeUsedInspector();
        inspector.IMPORT_CLASSES_ON_QF                  = true;
        inspector.USE_RELATIVE_QF                       = true;
        inspector.LOOK_ROOT_NS_UP                       = true;
        myFixture.configureByFile("testData/fixtures/lang/classConstant/class-name-constant-collisions.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/lang/classConstant/class-name-constant-collisions.fixed.php");
    }
    public void testConfigurationCase() {
        final ClassConstantCanBeUsedInspector inspector = new ClassConstantCanBeUsedInspector();
        inspector.IMPORT_CLASSES_ON_QF                  = true;
        inspector.USE_RELATIVE_QF                       = false;
        inspector.LOOK_ROOT_NS_UP                       = false;
        myFixture.configureByFile("testData/fixtures/lang/classConstant/configuration-class-definition.php");
        myFixture.configureByFile("testData/fixtures/lang/classConstant/configuration-class-reference.php");
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile(
                "testData/fixtures/lang/classConstant/configuration-class-reference.php",
                "testData/fixtures/lang/classConstant/configuration-class-reference.fixed.php",
                false
        );
    }
}
