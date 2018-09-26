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
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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
        this.files             = new CopyOnWriteArraySet<>();

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
                        .filter(file -> changeListManager.getStatus(file) == FileStatus.NOT_CHANGED)
                        .forEach(files::remove);
                /* catch up on branch change */
                files.addAll(changeListManager.getAffectedFiles());
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
