package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.SelfClassReferencingInspector;

final public class SelfClassReferencingInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testDefault() {
        SelfClassReferencingInspector selfClassReferencingInspector = new SelfClassReferencingInspector();
        selfClassReferencingInspector.PREFER_CLASS_NAMES            = false;
        myFixture.enableInspections(selfClassReferencingInspector);
        myFixture.configureByFile("testData/fixtures/codeStyle/self-class-referencing.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/codeStyle/self-class-referencing.fixed.php");
    }

    public void testReverse() {
        SelfClassReferencingInspector selfClassReferencingInspector = new SelfClassReferencingInspector();
        selfClassReferencingInspector.PREFER_CLASS_NAMES            = true;
        myFixture.enableInspections(selfClassReferencingInspector);
        myFixture.configureByFile("testData/fixtures/codeStyle/self-class-referencing.reverse.php");
        myFixture.testHighlighting(true, false, true);

        myFixture.getAllQuickFixes().forEach(fix -> myFixture.launchAction(fix));
        myFixture.setTestDataPath(".");
        myFixture.checkResultByFile("testData/fixtures/codeStyle/self-class-referencing.reverse.fixed.php");
    }
}
