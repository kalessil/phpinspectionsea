package com.kalessil.phpStorm.phpInspectionsEA.indexers;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NewCoreApiPolyfillsIndexer extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> identity = ID.create("kalessil.phpStorm.phpInspectionsEA.new_core_api_polyfills");
    private final KeyDescriptor<String> descriptor  = new EnumeratorStringDescriptor();

    private static final Set<String> functions      = new HashSet<>();
    static {
        functions.add("is_iterable");
        functions.add("is_countable");
        functions.add("str_contains");
        functions.add("str_starts_with");
        functions.add("str_ends_with");
    }

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
                    if (element instanceof Function) {
                        final String name = element.getName();
                        if (functions.contains(name)) {
                            final String fqn = element.getFQN();
                            if (fqn.equals("\\" + name)) {
                                final VirtualFile virtualFile = psiFile.getVirtualFile();
                                result.put(fqn, virtualFile == null ? "?" : virtualFile.getCanonicalPath());
                            }
                        }
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
        return 3;
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
