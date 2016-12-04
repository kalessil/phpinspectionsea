package com.kalessil.phpStorm.phpInspectionsEA.inspectors.apiUsage;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.config.PhpLanguageFeature;
import com.jetbrains.php.config.PhpLanguageLevel;
import com.jetbrains.php.config.PhpProjectConfigurationFacade;
import com.jetbrains.php.lang.psi.elements.FunctionReference;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.HashMap;
import java.util.Map;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class RandomApiMigrationInspector extends BasePhpInspection {
    // configuration flags automatically saved by IDE
    @SuppressWarnings("WeakerAccess")
    public boolean SUGGEST_USING_RANDOM_INT = true;

    private static final String messagePattern = "'%o%(...)' has recommended replacement '%n%(...)', consider migrating";

    @NotNull
    public String getShortName() {
        return "RandomApiMigrationInspection";
    }

    private static Map<String, String> mappingMt   = new HashMap<>();
    private static Map<String, String> mappingEdge = new HashMap<>();
    static {
        mappingMt.put("srand",        "mt_srand");
        mappingMt.put("getrandmax",   "mt_getrandmax");
        mappingMt.put("rand",         "mt_rand");

        mappingEdge.put("srand",      "mt_srand");
        mappingEdge.put("getrandmax", "mt_getrandmax");
        mappingEdge.put("rand",       "random_int");
        mappingEdge.put("mt_rand",    "random_int");
    }

    private Map<String, String> getMapping(PhpLanguageLevel phpVersion) {
        if (SUGGEST_USING_RANDOM_INT && phpVersion.hasFeature(PhpLanguageFeature.SCALAR_TYPE_HINTS)) {
            return mappingEdge;
        }

        return mappingMt;
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpFunctionCall(FunctionReference reference) {
                final String functionName = reference.getName();
                if (StringUtil.isEmpty(functionName)) {
                    return;
                }

                PhpLanguageLevel php = PhpProjectConfigurationFacade.getInstance(holder.getProject()).getLanguageLevel();
                Map<String, String> mapFunctions = getMapping(php);
                if (mapFunctions.containsKey(functionName)) {
                    String suggestedName = mapFunctions.get(functionName);
                    /* random_int needs 2 parameters always, so check if mt_rand can be suggested */
                    if (2 != reference.getParameters().length && suggestedName.equals("random_int")) {
                        if (functionName.equals("rand")) {
                            suggestedName = "mt_rand";
                        } else {
                            return;
                        }
                    }

                    final String message = messagePattern
                            .replace("%o%", functionName)
                            .replace("%n%", suggestedName);
                    holder.registerProblem(reference, message, ProblemHighlightType.LIKE_DEPRECATED, new TheLocalFix(suggestedName));
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return (new RandomApiMigrationInspector.OptionsPanel()).getComponent();
    }

    public class OptionsPanel {
        final private JPanel optionsPanel;

        final private JCheckBox suggestUsingRandomInt;

        public OptionsPanel() {
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new MigLayout());

            suggestUsingRandomInt = new JCheckBox("Suggest using random_int", SUGGEST_USING_RANDOM_INT);
            suggestUsingRandomInt.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    SUGGEST_USING_RANDOM_INT = suggestUsingRandomInt.isSelected();
                }
            });
            optionsPanel.add(suggestUsingRandomInt, "wrap");
        }

        public JPanel getComponent() {
            return optionsPanel;
        }
    }

    private static class TheLocalFix implements LocalQuickFix {
        final private String suggestedName;

        TheLocalFix(@NotNull String suggestedName) {
            super();
            this.suggestedName = suggestedName;
        }

        @NotNull
        @Override
        public String getName() {
            return "Use newer function";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement();
            if (expression instanceof FunctionReference) {
                ((FunctionReference) expression).handleElementRename(this.suggestedName);
            }
        }
    }
}
