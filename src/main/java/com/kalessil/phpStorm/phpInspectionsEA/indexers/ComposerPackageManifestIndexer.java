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

public class ComposerPackageManifestIndexer extends FileBasedIndexExtension<String, String> {
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
                Stream.of("require", "require-dev").forEach(name -> {
                    final JsonProperty property = manifest.findProperty(name);
                    if (property != null) {
                        final JsonValue value = property.getValue();
                        if (value instanceof JsonObject) {
                            ((JsonObject) value).getPropertyList().forEach(entry -> dependencies.add(entry.getName()));
                        }
                    }
                });
            }

            final String key = file.getFile().getCanonicalPath();
            if (key != null) {
                final Map<String, String> result = new THashMap<>();
                result.put(key, String.format("%s:%s", packageName, String.join(",", dependencies)));
                dependencies.clear();

                return result;
            }
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
        return 2;
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

