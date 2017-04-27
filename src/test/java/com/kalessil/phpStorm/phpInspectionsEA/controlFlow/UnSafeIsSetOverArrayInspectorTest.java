package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage.UnSafeIsSetOverArrayInspector;
import org.jetbrains.annotations.NotNull;

final public class UnSafeIsSetOverArrayInspectorTest extends CodeInsightFixtureTestCase {
    @NotNull
    private UnSafeIsSetOverArrayInspector getInspector() {
        UnSafeIsSetOverArrayInspector inspector = new UnSafeIsSetOverArrayInspector();
        inspector.SUGGEST_TO_USE_ARRAY_KEY_EXISTS = true;
        inspector.SUGGEST_TO_USE_NULL_COMPARISON = true;
        return inspector;
    }

    public void testIfFindsScalarAndArrayPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/isset-over-scalars-and-arrays.php");
        myFixture.enableInspections(getInspector());
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsClassPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/isset-over-class.php");
        myFixture.enableInspections(getInspector());
        myFixture.testHighlighting(true, false, true);
    }
    public void testIfFindsPropertyPatterns() {
        myFixture.configureByFile("fixtures/controlFlow/isset-on-properties.php");
        myFixture.enableInspections(getInspector());
        myFixture.testHighlighting(true, false, true);
    }
}
