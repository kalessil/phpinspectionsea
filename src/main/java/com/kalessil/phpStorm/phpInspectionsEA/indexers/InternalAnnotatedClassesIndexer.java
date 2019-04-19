package com.kalessil.phpStorm.phpInspectionsEA.indexers;

import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class InternalAnnotatedClassesIndexer extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> identity = ID.create("kalessil.phpStorm.phpInspectionsEA.internal_classes");
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
            final PsiFile psiFile = file.getPsiFile();
            if (psiFile instanceof PhpFile) {
                final Map<String, String> result = new THashMap<>();
                for (final PhpNamedElement element : ((PhpFile) psiFile).getTopLevelDefs().values()) {
                    if (element instanceof PhpClass && element.isInternal()) {
                        result.put(element.getFQN(), "");
                    }
                }
                return result;
            }
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
        return file -> file.getFileType() == PhpFileType.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}

