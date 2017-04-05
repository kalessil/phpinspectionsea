package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.NamingConvention.NamingConventionInspector;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.NamingConvention.NamingRule;


public class NamingConventionInspectorTest extends CodeInsightFixtureTestCase {

    public void testCheckClass() {
        myFixture.configureByFile("fixtures/codeStyle/naming-convention/check-class.php");
        testRule(new NamingRule("^[A-Z]{1}[a-z_]+$", NamingRule.TYPE_CLASS));
    }

    public void testCheckAbstract() {
        myFixture.configureByFile("fixtures/codeStyle/naming-convention/check-abstract.php");
        testRule(new NamingRule("^Custom.+Abstract$", NamingRule.TYPE_ABSTRACT));
    }

    public void testCheckFinal() {
        myFixture.configureByFile("fixtures/codeStyle/naming-convention/check-final.php");
        testRule(new NamingRule("^Final_[a-zA-Z_]+$", NamingRule.TYPE_FINAL));
    }

    public void testCheckInterface() {
        myFixture.configureByFile("fixtures/codeStyle/naming-convention/check-interface.php");
        testRule(new NamingRule("[a-zA-Z_]+Interface$", NamingRule.TYPE_INTERFACE));
    }

    public void testCheckTrait() {
        myFixture.configureByFile("fixtures/codeStyle/naming-convention/check-trait.php");
        testRule(new NamingRule(".+Trait.+", NamingRule.TYPE_TRAIT));
    }




    public void testExtendsClass() {
        myFixture.configureByFile("fixtures/codeStyle/naming-convention/extend-class.php");
        testRule(new NamingRule(".+Controller$", NamingRule.TYPE_CLASS, "\\BaseController"));
    }


    private void testRule(NamingRule rule) {
        NamingConventionInspector inspector = new NamingConventionInspector();
        inspector.rules.add(rule);
        myFixture.enableInspections(inspector);
        myFixture.testHighlighting(true, false, true);
    }
}