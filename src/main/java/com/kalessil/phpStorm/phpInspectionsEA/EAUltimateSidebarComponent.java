package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.kalessil.phpStorm.phpInspectionsEA.license.LicenseService;
import com.kalessil.phpStorm.phpInspectionsEA.settings.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Collections;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class EAUltimateSidebarComponent extends AbstractProjectComponent {
    final private String TOOL_WINDOW_ID = "PHP Inspections";
    final private ToolWindowManager windowManager;

    protected EAUltimateSidebarComponent(@NotNull Project project) {
        super(project);
        windowManager = ToolWindowManager.getInstance(project);
    }

    @Override
    public void projectOpened() {
        if (!isInstantiated()) {
            final ToolWindow window = windowManager.registerToolWindow(TOOL_WINDOW_ID, buildPanel(), ToolWindowAnchor.RIGHT);
            window.setIcon(new ImageIcon(getClass().getResource("/logo_15x15.png")));
            window.setTitle("project settings");
        }
    }

    private void refresh() {
        ApplicationManager.getApplication().runWriteAction(() -> {
            final PsiDocumentManager documents = PsiDocumentManager.getInstance(myProject);
            final PsiManager files             = PsiManager.getInstance(myProject);
            for (final VirtualFile file : FileEditorManager.getInstance(myProject).getOpenFiles()) {
                final PsiFile psi = files.findFile(file);
                if (psi != null) {
                    /* psi.subtreeChanged hangs and does nothing, hence hammer-scenario */
                    final Document document = documents.getDocument(psi);
                    if (document != null) {
                        documents.doPostponedOperationsAndUnblockDocument(document);
                        documents.reparseFiles(Collections.singletonList(file), false);
                    }
                }
            }
        });
    }

    @NotNull
    private JPanel buildPanel() {
        return OptionsComponent.create(component -> {
            final EAUltimateProjectSettings s = myProject.getComponent(EAUltimateProjectSettings.class);
            component.addPanel("License information",                           panel -> {
                String message               = "Obtaining license information...";
                final LicenseService service = EAUltimateApplicationComponent.getLicenseService();
                if (service != null && service.shouldCheckPluginLicense()) {
                    final int total = 260;
                    final int basic = 146;
                    try {
                        if (service.isActivatedLicense()) {
                            message = service.isActiveLicense()
                                    ? String.format("Active license (running all features: %s inspections)", total)
                                    : String.format("Expired license (running basic features: %s of %s inspections)", basic, total);
                        } else if (service.isTrialLicense()) {
                            message = service.isActiveTrialLicense()
                                    ? String.format("Active trial (running all features: %s inspections).", total)
                                    : String.format("Expired trial (running basic features: %s of %s inspections)", basic, total);
                        } else {
                            message = String.format("Not activated (running basic features: %s of %s inspections)", basic, total);
                        }
                    } catch (final Exception failure) {
                        message = String.format("Licensing information is not available (error: %s)", failure.getMessage());
                    }
                }
                panel.addText("", 12);
                panel.addText(message);
            });
            component.addPanel("Settings management",                            panel -> {
                panel.addText("", 12);
                panel.addHyperlink("File / Settings / Editor / Inspections", (event) -> ShowSettingsUtil.getInstance().showSettingsDialog(myProject, "Inspections"));
                panel.addCheckbox("Prefer Yoda comparison style",            s.isPreferringYodaComparisonStyle(), (is) -> { s.setPreferringYodaComparisonStyle(is); this.refresh(); });
                panel.addCheckbox("Analyze only modified files",             s.isAnalyzingOnlyModifiedFiles(),    (is) -> { s.setAnalyzingOnlyModifiedFiles(is);    this.refresh(); });
            });
            component.addPanel("Strictness categories * (loosest to strictest)", panel -> {
                panel.addText("", 12);
                panel.addCheckbox("Prio 1: Security",                 s.isCategoryActive(StrictnessCategory.STRICTNESS_CATEGORY_SECURITY),                 (is) -> { s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_SECURITY, is);                 this.refresh(); });
                panel.addCheckbox("Prio 2: Probable bugs",            s.isCategoryActive(StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS),            (is) -> { s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS, is);            this.refresh(); });
                panel.addCheckbox("Prio 3: Performance",              s.isCategoryActive(StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE),              (is) -> { s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE, is);              this.refresh(); });
                panel.addCheckbox("Prio 4: Architecture, PHPUnit",    s.isCategoryActive(StrictnessCategory.STRICTNESS_CATEGORY_ARCHITECTURE),             (is) -> {
                                                                                                                                                                s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_ARCHITECTURE, is);
                                                                                                                                                                s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_PHPUNIT, is);
                                                                                                                                                                this.refresh();
                                                                                                                                                            });
                panel.addCheckbox("Prio 5: Control flow",             s.isCategoryActive(StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW),             (is) -> { s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW, is);             this.refresh(); });
                panel.addCheckbox("Prio 6: Language level migration", s.isCategoryActive(StrictnessCategory.STRICTNESS_CATEGORY_LANGUAGE_LEVEL_MIGRATION), (is) -> { s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_LANGUAGE_LEVEL_MIGRATION, is); this.refresh(); });
                panel.addCheckbox("Prio 7: Code style",               s.isCategoryActive(StrictnessCategory.STRICTNESS_CATEGORY_CODE_STYLE),               (is) -> { s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_CODE_STYLE, is);               this.refresh(); });
                panel.addCheckbox("Prio 8: Unused",                   s.isCategoryActive(StrictnessCategory.STRICTNESS_CATEGORY_UNUSED),                   (is) -> { s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_UNUSED, is);                   this.refresh(); });
                panel.addText("", 12);
                panel.addText("* inspections from the unchecked groups are skipped", 12);
            });
        });
    }

    @Override
    public void projectClosed() {
        if (this.isInstantiated() && ! this.myProject.isDisposed()) {
            windowManager.unregisterToolWindow(TOOL_WINDOW_ID);
        }
    }

    private boolean isInstantiated() {
        return windowManager.getToolWindow(TOOL_WINDOW_ID) != null;
    }
}
