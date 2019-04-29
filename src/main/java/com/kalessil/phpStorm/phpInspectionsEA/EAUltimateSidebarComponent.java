package com.kalessil.phpStorm.phpInspectionsEA;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.php.lang.psi.PhpFile;
import com.kalessil.phpStorm.phpInspectionsEA.license.LicenseService;
import com.kalessil.phpStorm.phpInspectionsEA.settings.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;

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
        //final FileDocumentManager manager = FileDocumentManager.getInstance();
        for (final VirtualFile file : FileEditorManager.getInstance(myProject).getOpenFiles()) {
            final PsiFile psi = PsiManager.getInstance(myProject).findFile(file);
            if (psi instanceof PhpFile) {
                psi.subtreeChanged();
            }
//            final Document document = manager.getDocument(file);
//            if (document != null && file.exists()) {
//                manager.saveDocumentAsIs(document);
//                (new File(file.getPath())).setLastModified(System.currentTimeMillis() + 1L);
//                manager.reloadFromDisk(document);
//            }
        }
    }

    @NotNull
    private JPanel buildPanel() {
        return OptionsComponent.create(component -> {
            final EAUltimateProjectSettings s = myProject.getComponent(EAUltimateProjectSettings.class);
            component.addPanel("License information",                           panel -> {
                String message               = "Licensing information is not available";
                final LicenseService service = EAUltimateApplicationComponent.getLicenseService();
                if (service != null && service.shouldCheckPluginLicense()) {
                    try {
                        if (service.isActivatedLicense()) {
                            message = "Activated (running all features)";
                        } else if (service.isTrialLicense()) {
                            message = service.isActiveTrialLicense() ? "Active trial (running all features)." : "Expired trial (partially suspended features)";
                        } else {
                            message = "Not activated (partially suspended features)";
                        }
                    } catch (final Exception failure) { /* do nothing */ }
                }
                panel.addText("", 12);
                panel.addText(message + ", as of IDE start.");
            });
            component.addPanel("Settings management",                            panel -> {
                panel.addText("", 12);
                panel.addHyperlink("File / Settings / Editor / Inspections", (event) -> ShowSettingsUtil.getInstance().showSettingsDialog(myProject, "Inspections"));
                panel.addCheckbox("Prefer Yoda comparison style",            s.isPreferringYodaComparisonStyle(), (is) -> { s.setPreferringYodaComparisonStyle(is); this.refresh(); });
                panel.addCheckbox("Analyze only modified files",             s.isAnalyzingOnlyModifiedFiles(),    (is) -> { s.setAnalyzingOnlyModifiedFiles(is);    this.refresh(); });
            });
            component.addPanel("Strictness categories * (loosest to strictest)", panel -> {
                panel.addText("", 12);
                panel.addCheckbox("Prio 1: Security",                 s.isCategoryActive(StrictnessCategory.STRICTNESS_CATEGORY_SECURITY),                 (is) -> s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_SECURITY, is));
                panel.addCheckbox("Prio 2: Probable bugs",            s.isCategoryActive(StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS),            (is) -> s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_PROBABLE_BUGS, is));
                panel.addCheckbox("Prio 3: Performance",              s.isCategoryActive(StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE),              (is) -> s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_PERFORMANCE, is));
                panel.addCheckbox("Prio 4: Architecture, PhpUnit",    s.isCategoryActive(StrictnessCategory.STRICTNESS_CATEGORY_ARCHITECTURE),             (is) -> {
                                                                                                                                                                s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_ARCHITECTURE, is);
                                                                                                                                                                s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_PHPUNIT, is);
                                                                                                                                                            });
                panel.addCheckbox("Prio 5: Control flow",             s.isCategoryActive(StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW),             (is) -> s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_CONTROL_FLOW, is));
                panel.addCheckbox("Prio 6: Language level migration", s.isCategoryActive(StrictnessCategory.STRICTNESS_CATEGORY_LANGUAGE_LEVEL_MIGRATION), (is) -> s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_LANGUAGE_LEVEL_MIGRATION, is));
                panel.addCheckbox("Prio 7: Code style",               s.isCategoryActive(StrictnessCategory.STRICTNESS_CATEGORY_CODE_STYLE),               (is) -> s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_CODE_STYLE, is));
                panel.addCheckbox("Prio 8: Unused",                   s.isCategoryActive(StrictnessCategory.STRICTNESS_CATEGORY_UNUSED),                   (is) -> s.setCategoryActiveFlag(StrictnessCategory.STRICTNESS_CATEGORY_UNUSED, is));
                panel.addText("", 12);
                panel.addText("* inspections from the unchecked groups are skipped", 12);
            });
        });
    }

    @Override
    public void projectClosed() {
        if (isInstantiated()) {
            windowManager.unregisterToolWindow(TOOL_WINDOW_ID);
        }
    }

    private boolean isInstantiated() {
        return windowManager.getToolWindow(TOOL_WINDOW_ID) != null;
    }
}
