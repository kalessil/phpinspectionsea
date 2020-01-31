package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.changes.ChangeListAdapter;
import com.intellij.openapi.vcs.changes.ChangeListListener;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ConcurrentHashSet;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class EAUltimateChangesTrackerComponent extends AbstractProjectComponent {
    private final Set<VirtualFile> files;
    private final DocumentListener documentListener;
    private final ChangeListListener changeListListener;

    private FileDocumentManager documentManager;
    private ChangeListManager changeListManager;

    protected EAUltimateChangesTrackerComponent(@NotNull Project project) {
        super(project);

        this.documentManager   = FileDocumentManager.getInstance();
        this.changeListManager = ChangeListManager.getInstance(project);
        this.files             = ContainerUtil.newConcurrentSet();

        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(this.documentListener = new DocumentAdapter() {
            @Override
            public void beforeDocumentChange(@NotNull DocumentEvent event) {
                /* we need to know files has been changed before inspections are getting invoked */
                final VirtualFile file = documentManager.getFile(event.getDocument());
                if (file != null) {
                    files.add(file);
                }
            }
        });

        this.changeListManager.addChangeListListener(this.changeListListener = new ChangeListAdapter() {
            public void changeListUpdateDone() {
                /* catch up on reverts */
                files.stream()
                        .filter(file -> {
                            final FileStatus status = changeListManager.getStatus(file);
                            return status != FileStatus.MODIFIED && status != FileStatus.ADDED;
                        })
                        .forEach(files::remove);
                /* catch up on branch change */
                changeListManager.getAffectedFiles().stream()
                        .filter(file -> {
                            final FileStatus status = changeListManager.getStatus(file);
                            return status == FileStatus.MODIFIED || status == FileStatus.ADDED;
                        })
                        .forEach(files::add);
            }
        });
    }

    @Override
    public void projectOpened() {
        super.projectOpened();

        /* pre-load all know changes */
        files.clear();
        files.addAll(this.changeListManager.getAffectedFiles());
    }

    @Override
    public void projectClosed() {
        super.projectClosed();

        files.clear();
        EditorFactory.getInstance().getEventMulticaster().removeDocumentListener(this.documentListener);
        this.changeListManager.removeChangeListListener(this.changeListListener);

        /* this solves objects leaking issues in older PhpStorm versions */
        this.documentManager   = null;
        this.changeListManager = null;
    }

    public boolean isChanged(@NotNull VirtualFile file) {
        return files.contains(file);
    }
}
