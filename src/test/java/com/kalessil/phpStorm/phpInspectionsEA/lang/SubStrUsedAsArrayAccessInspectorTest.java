package com.kalessil.phpStorm.phpInspectionsEA.lang;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.strpos.SubStrUsedAsArrayAccessInspector;

final public class SubStrUsedAsArrayAccessInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/lang/substr-used-as-index-access.php");
        myFixture.enableInspections(SubStrUsedAsArrayAccessInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}