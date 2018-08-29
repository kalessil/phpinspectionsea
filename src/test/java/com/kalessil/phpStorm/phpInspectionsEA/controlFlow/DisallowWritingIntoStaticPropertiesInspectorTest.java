package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.kalessil.phpStorm.phpInspectionsEA.PhpCodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeStyle.DisallowWritingIntoStaticPropertiesInspector;

final public class DisallowWritingIntoStaticPropertiesInspectorTest extends PhpCodeInsightFixtureTestCase {
    public void testAllowWriteFromSourceClassOnly() {
        final DisallowWritingIntoStaticPropertiesInspector inspector = new DisallowWritingIntoStaticPropertiesInspector();
        inspector.ALLOW_WRITE_FROM_SOURCE_CLASS                      = true;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/controlFlow/disallow-write-into-static-property-default.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testDisallowAnyWrites() {
        final DisallowWritingIntoStaticPropertiesInspector inspector = new DisallowWritingIntoStaticPropertiesInspector();
        inspector.ALLOW_WRITE_FROM_SOURCE_CLASS                      = false;
        myFixture.enableInspections(inspector);
        myFixture.configureByFile("testData/fixtures/controlFlow/disallow-any-write-into-static-property.php");
        myFixture.testHighlighting(true, false, true);
    }
}
