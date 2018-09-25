package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class EAUltimateChangesTrackerComponent extends AbstractProjectComponent {
    private final Set<VirtualFile> files;
    private FileDocumentManager manager;
    private Project project;

    protected EAUltimateChangesTrackerComponent(@NotNull Project project) {
        super(project);

        this.manager = FileDocumentManager.getInstance();
        this.project = project;
        this.files   = new CopyOnWriteArraySet<>();

        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new DocumentListener() {
            @Override
            public void beforeDocumentChange(@NotNull DocumentEvent event) {
                final VirtualFile file = manager.getFile(event.getDocument());
                if (file != null) {
                    files.add(file);
                }
            }

            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
            }
        });
    }

    @Override
    public void projectOpened() {
        super.projectOpened();

        files.clear();
        if (!project.isDisposed()) {
            files.addAll(ChangeListManager.getInstance(project).getAffectedFiles());
        }
    }

    @Override
    public void projectClosed() {
        super.projectClosed();

        files.clear();
//        this.project = null;
//        this.manager = null;
    }

    public boolean isChanged(@NotNull VirtualFile file) {
        return files.contains(file);
    }
}
