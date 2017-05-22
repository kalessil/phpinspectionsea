package com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;

public class MagicNumberInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new MagicNumberInspector());

        myFixture.configureByFile("fixtures/codeSmell/MagicNumber.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testOptionCheckOnMultiply() {
        final MagicNumberInspector magicNumberInspector = new MagicNumberInspector();
        magicNumberInspector.optionCheckOnMultiplier = false;

        myFixture.enableInspections(magicNumberInspector);

        myFixture.configureByFile("fixtures/codeSmell/MagicNumber.optionCheckOnMultiply-false.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testOptionCheckOnProperties() {
        final MagicNumberInspector magicNumberInspector = new MagicNumberInspector();
        magicNumberInspector.optionCheckOnProperties = false;

        myFixture.enableInspections(magicNumberInspector);

        myFixture.configureByFile("fixtures/codeSmell/MagicNumber.optionCheckOnProperties-false.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testOptionCheckOnParameters() {
        final MagicNumberInspector magicNumberInspector = new MagicNumberInspector();
        magicNumberInspector.optionCheckOnParameters = false;

        myFixture.enableInspections(magicNumberInspector);

        myFixture.configureByFile("fixtures/codeSmell/MagicNumber.optionCheckOnParameters-false.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testOptionCheckOnArguments() {
        final MagicNumberInspector magicNumberInspector = new MagicNumberInspector();
        magicNumberInspector.optionCheckOnArguments = false;

        myFixture.enableInspections(magicNumberInspector);

        myFixture.configureByFile("fixtures/codeSmell/MagicNumber.optionCheckOnArguments-false.php");
        myFixture.testHighlighting(true, false, true);
    }
}
