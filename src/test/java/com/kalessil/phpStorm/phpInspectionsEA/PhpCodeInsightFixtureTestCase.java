package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.openapi.module.WebModuleType;
import com.intellij.testFramework.builders.ModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import com.intellij.testFramework.fixtures.impl.ModuleFixtureBuilderImpl;
import com.intellij.testFramework.fixtures.impl.ModuleFixtureImpl;

import java.io.File;

public abstract class PhpCodeInsightFixtureTestCase extends CodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.setTestDataPath((new File("testData/..")).getAbsolutePath());
    }

    @Override
    protected Class getModuleBuilderClass() {
        return WebModuleFixtureBuilder.class;
    }

    static {
        IdeaTestFixtureFactory.getFixtureFactory().registerFixtureBuilder(WebModuleFixtureBuilder.class, WebModuleFixtureBuilderImpl.class);
    }

    private interface WebModuleFixtureBuilder<T extends ModuleFixture> extends ModuleFixtureBuilder<T> {
    }

    private static class WebModuleFixtureBuilderImpl extends ModuleFixtureBuilderImpl implements WebModuleFixtureBuilder {
        WebModuleFixtureBuilderImpl(final TestFixtureBuilder<? extends IdeaProjectTestFixture> fixtureBuilder) {
            super(new WebModuleType(), fixtureBuilder);
        }

        @Override
        protected ModuleFixture instantiateFixture() {
            return new ModuleFixtureImpl(this);
        }
    }
}
