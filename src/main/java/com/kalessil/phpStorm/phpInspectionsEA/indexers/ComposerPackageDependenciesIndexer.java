package com.kalessil.phpStorm.phpInspectionsEA.indexers;

import com.intellij.json.psi.JsonObject;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import com.intellij.psi.PsiElement;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class ComposerPackageDependenciesIndexer extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> identity = ID.create("kalessil.phpStorm.phpInspectionsEA.packages");
    private final KeyDescriptor<String> descriptor  = new EnumeratorStringDescriptor();

    @NotNull
    @Override
    public ID<String, String> getName() {
        return identity;
    }

    @NotNull
    @Override
    public DataIndexer<String, String, FileContent> getIndexer() {
        return file -> {
            String packageName              = "";
            final List<String> packages     = new ArrayList<>();
            final List<String> dependencies = new ArrayList<>();

            final PsiElement content = file.getPsiFile().getFirstChild();
            if (content instanceof JsonObject) {
                final JsonObject manifest = (JsonObject) content;

                /* extract package name */
                final JsonProperty nameProperty = manifest.findProperty("name");
                if (nameProperty != null) {
                    final JsonValue name = nameProperty.getValue();
                    if (name instanceof JsonStringLiteral) {
                        packageName = ((JsonStringLiteral) name).getValue();
                    }
                }

                /* extract packages */
                packages.add(packageName);
                Stream.of("require", "require-dev", "replace").forEach(sectionName -> {
                    final JsonProperty property = manifest.findProperty(sectionName);
                    if (property != null) {
                        final JsonValue value = property.getValue();
                        if (value instanceof JsonObject) {
                            final List<JsonProperty> list = ((JsonObject) value).getPropertyList();
                            if (sectionName.equals("replace")) {
                                list.forEach(entry -> packages.add(entry.getName()));
                            } else {
                                list.forEach(entry -> dependencies.add(entry.getName()));
                            }
                        }
                    }
                });
            }

            final String key = file.getFile().getCanonicalPath();
            if (key != null) {
                final Map<String, String> result = new THashMap<>(1);
                result.put(key, String.format("%s:%s", String.join(",", packages), String.join(",", dependencies)));
                packages.clear();
                dependencies.clear();

                return result;
            }
            packages.clear();
            dependencies.clear();

            return new THashMap<>();
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return descriptor;
    }

    @NotNull
    @Override
    public DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> file.getName().equals("composer.json");
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}

