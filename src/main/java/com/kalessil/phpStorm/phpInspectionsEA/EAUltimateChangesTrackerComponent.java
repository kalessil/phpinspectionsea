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
    private static final Set<VirtualFile> files = new CopyOnWriteArraySet<>();

    private final FileDocumentManager manager;

    protected EAUltimateChangesTrackerComponent(@NotNull Project project) {
        super(project);
        manager = FileDocumentManager.getInstance();

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
        files.clear();
        files.addAll(ChangeListManager.getInstance(myProject).getAffectedFiles());
    }

    @Override
    public void projectClosed() {
        files.clear();
    }

    public boolean isChanged(@NotNull VirtualFile file) {
        return files.contains(file);
    }
}
