package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.openapi.module.WebModuleType;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.testFramework.builders.ModuleFixtureBuilder;
import com.intellij.testFramework.fixtures.*;
import com.intellij.testFramework.fixtures.impl.ModuleFixtureBuilderImpl;
import com.intellij.testFramework.fixtures.impl.ModuleFixtureImpl;
import com.kalessil.phpStorm.phpInspectionsEA.settings.ComparisonStyle;

import java.io.File;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class PhpCodeInsightFixtureTestCase extends CodeInsightFixtureTestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        /* platform backward-compatibility */
        myFixture.setTestDataPath((new File("testData/..")).getAbsolutePath());

        /* prevent failing tests to cause false-alarms where the setting suppose to be reset */
        ComparisonStyle.force(ComparisonStyle.REGULAR);
    }

    @Override
    protected Class getModuleBuilderClass() {
        return WebModuleFixtureBuilder.class;
    }

    static {
        IdeaTestFixtureFactory.getFixtureFactory().registerFixtureBuilder(WebModuleFixtureBuilder.class, WebModuleFixtureBuilderImpl.class);

        /* gradle with its' caches can be located not only in user folder (e.g. another partition) */
        Stream.of(System.getenv("GRADLE_USER_HOME"), System.getenv("GRADLE_HOME"))
                .filter(Objects::nonNull)
                .forEach(path -> VfsRootAccess.allowRootAccess(() -> {}, path + "/caches"));
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
