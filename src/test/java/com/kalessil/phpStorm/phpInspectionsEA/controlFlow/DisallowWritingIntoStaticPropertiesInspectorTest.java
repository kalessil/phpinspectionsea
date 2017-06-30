package com.kalessil.phpStorm.phpInspectionsEA.controlFlow;

import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.codeSmell.DisallowWritingIntoStaticPropertiesInspector;

final public class DisallowWritingIntoStaticPropertiesInspectorTest extends CodeInsightFixtureTestCase {
    public void testAllowWriteFromSourceClassOnly() {
        DisallowWritingIntoStaticPropertiesInspector inspector = new DisallowWritingIntoStaticPropertiesInspector();
        inspector.ALLOW_WRITE_FROM_SOURCE_CLASS                = true;

        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/controlFlow/disallow-write-into-static-property-default.php");
        myFixture.testHighlighting(true, false, true);
    }

    public void testDisallowAnyWrites() {
        DisallowWritingIntoStaticPropertiesInspector inspector = new DisallowWritingIntoStaticPropertiesInspector();
        inspector.ALLOW_WRITE_FROM_SOURCE_CLASS                = false;

        myFixture.enableInspections(inspector);

        myFixture.configureByFile("fixtures/controlFlow/disallow-any-write-into-static-property.php");
        myFixture.testHighlighting(true, false, true);
    }
}
