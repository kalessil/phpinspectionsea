package com.kalessil.phpStorm.phpInspectionsEA.indexers;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.php.lang.PhpFileType;
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

final public class ComposerPackageRelationIndexer extends FileBasedIndexExtension<String, String> {
    public static final ID<String, String> identity = ID.create("kalessil.phpStorm.phpInspectionsEA.file_to_package");
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
            final VirtualFile rootFolder = file.getProject().getBaseDir();
            /* up-wards scan for manifest or project root */
            VirtualFile manifest = null, currentFolder = file.getFile().getParent();
            while (currentFolder != null) {
                manifest = currentFolder.findChild("composer.json");
                if (manifest != null || currentFolder.equals(rootFolder)) {
                    break;
                }
                currentFolder = currentFolder.getParent();
            }
            /* storing the file - manifest association */
            final String key = file.getFile().getCanonicalPath();
            if (key != null) {
                final String value = manifest == null ? null : manifest.getCanonicalPath();
                if (value != null) {
                    final Map<String, String> result = new THashMap<>(1);
                    result.put(key, value);
                    return result;
                }
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
        return 4;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> file.getFileType() == PhpFileType.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return false;
    }
}
