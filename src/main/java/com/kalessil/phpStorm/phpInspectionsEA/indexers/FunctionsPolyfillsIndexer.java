package com.kalessil.phpStorm.phpInspectionsEA.indexers;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
import com.jetbrains.php.lang.psi.PhpFile;
import com.jetbrains.php.lang.psi.elements.Function;
import com.jetbrains.php.lang.psi.elements.PhpNamedElement;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.PhpLanguageLevel;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class FunctionsPolyfillsIndexer extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> identity = ID.create("kalessil.phpStorm.phpInspectionsEA.functions_polyfills");
    private final KeyDescriptor<String> descriptor  = new EnumeratorStringDescriptor();

    private static final Map<String, PhpLanguageLevel> functions = new HashMap<>();
    static {
        functions.put("\\is_iterable",     PhpLanguageLevel.PHP710);
        functions.put("\\is_countable",    PhpLanguageLevel.PHP740);
        functions.put("\\str_contains",    PhpLanguageLevel.PHP800);
        functions.put("\\str_starts_with", PhpLanguageLevel.PHP800);
        functions.put("\\str_ends_with",   PhpLanguageLevel.PHP800);
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
                        final String fqn = element.getFQN();
                        if (functions.containsKey(fqn)) {
                            final String location  = file.getFile().getCanonicalPath();
                            final boolean isTarget = location != null && ! location.contains(".jar!") && ! location.contains("/stubs/");
                            if (isTarget) {
                                result.put(fqn, location);
                            }
                        }
                    }
                }
                return result;
            }
            return new THashMap<>();
        };
    }

    public static boolean isFunctionAvailable(@NotNull String fqn, @NotNull Project project) {
        if (functions.containsKey(fqn)) {
            return PhpLanguageLevel.get(project).atLeast(functions.get(fqn)) ||
                   ! FileBasedIndex.getInstance().getValues(identity, fqn, GlobalSearchScope.allScope(project)).isEmpty();
        }
        return false;
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
