package com.kalessil.phpStorm.phpInspectionsEA.license;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;

final public class DeactivateLicenseAction extends AnAction implements DumbAware {
    public DeactivateLicenseAction() {
        /* TODO: get ea app component -> get license server*/
    }

    public void update(AnActionEvent event) {
        final Presentation presentation = event.getPresentation();
        presentation.setVisible(true);
        presentation.setEnabled(false);
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        // deactivate
    }
}
