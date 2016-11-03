package com.kalessil.phpStorm.phpInspectionsEA.codeStyle;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.languageConstructions.StaticInvocationViaThisInspector;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FixturesLocationUtil;

public class StaticInvocationViaThisInspectorTest extends CodeInsightFixtureTestCase {
    @Override
    protected String getBasePath() {
        return FileUtil.toSystemDependentName(FixturesLocationUtil.RELATIVE_TEST_DATA_PATH);
    }

    public void testIfFindsAllPatterns() {
        myFixture.configureByFile("fixtures/static-method-invocation-via-this.php");
        myFixture.enableInspections(StaticInvocationViaThisInspector.class);
        myFixture.testHighlighting(true, false, true);
    }
}
