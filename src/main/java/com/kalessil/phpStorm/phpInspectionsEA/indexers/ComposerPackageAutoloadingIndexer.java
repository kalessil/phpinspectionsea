package com.kalessil.phpStorm.phpInspectionsEA.indexers;

import com.intellij.json.JsonFileType;
import com.intellij.json.psi.*;
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

public class ComposerPackageAutoloadingIndexer extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> identity = ID.create("kalessil.phpStorm.phpInspectionsEA.package_autoloading");
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
            final List<String> autoloading = new ArrayList<>();

            if (this.getInputFilter().acceptInput(file.getFile())) {
                final PsiElement content = file.getPsiFile().getFirstChild();
                if (content instanceof JsonObject) {
                    final JsonObject manifest = (JsonObject) content;

                    /* extract autoloading information */
                    Stream.of("autoload", "autoload-dev").forEach(sectionName -> {
                        final JsonProperty property = manifest.findProperty(sectionName);
                        if (property != null) {
                            final JsonValue value = property.getValue();
                            if (value instanceof JsonObject) {
                                /* iterate autoloading types and pick uot psr-0 and psr-4 */
                                ((JsonObject) value).getPropertyList().forEach(type -> {
                                    final String typeName = type.getName().toLowerCase();
                                    if (typeName.equals("psr-4") || typeName.equals("psr-0")) {
                                        final JsonValue mapping = type.getValue();
                                        if (mapping instanceof JsonObject) {
                                            /* iterate namespaces and extract possible locations */
                                            ((JsonObject) mapping).getPropertyList().forEach(mappingEntry -> {
                                                final String namespace = mappingEntry.getName();
                                                if (!namespace.isEmpty()) {
                                                    final List<String> extractedLocations = new ArrayList<>();
                                                    final JsonValue locations             = mappingEntry.getValue();
                                                    if (locations instanceof JsonStringLiteral) {
                                                        extractedLocations.add(((JsonStringLiteral) locations).getValue());
                                                    } else if (locations instanceof JsonArray) {
                                                        ((JsonArray) locations).getValueList().stream()
                                                                .filter(location  -> location instanceof JsonStringLiteral)
                                                                .forEach(location -> extractedLocations.add(((JsonStringLiteral) location).getValue()));
                                                    }
                                                    if (!extractedLocations.isEmpty()) {
                                                        extractedLocations.removeIf(String::isEmpty);
                                                        if (!extractedLocations.isEmpty()) {
                                                            autoloading.add(String.format("%s:%s", namespace, String.join(",", extractedLocations)));
                                                            extractedLocations.clear();
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }

            final String key = file.getFile().getCanonicalPath();
            if (key != null) {
                final Map<String, String> result = new THashMap<>();
                autoloading.forEach(mappingEntry -> result.put(key, mappingEntry));
                autoloading.clear();

                return result;
            }
            autoloading.clear();

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
        return 4;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> file.getName().equals("composer.json") && file.getFileType() == JsonFileType.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}

