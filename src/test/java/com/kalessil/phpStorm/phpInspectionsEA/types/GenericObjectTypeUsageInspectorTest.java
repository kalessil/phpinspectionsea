package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpDoc.GenericObjectTypeUsageInspector;

final public class GenericObjectTypeUsageInspectorTest extends CodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(GenericObjectTypeUsageInspector.class);

        myFixture.configureByFile("fixtures/types/type-object-annotation.php");
        myFixture.testHighlighting(true, false, true);
    }
}
