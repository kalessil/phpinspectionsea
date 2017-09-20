package com.kalessil.phpStorm.phpInspectionsEA.types;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpDoc.GenericObjectTypeUsageInspector;

final public class GenericObjectTypeUsageInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testIfFindsAllPatterns() {
        myFixture.enableInspections(new GenericObjectTypeUsageInspector());
        myFixture.configureByFile("fixtures/types/type-object-annotation.php");
        myFixture.testHighlighting(true, false, true);
    }
}
