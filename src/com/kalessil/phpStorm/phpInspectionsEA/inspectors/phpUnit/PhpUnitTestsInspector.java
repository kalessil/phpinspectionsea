package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.PhpLangUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocRef;
import com.jetbrains.php.lang.documentation.phpdoc.psi.impl.PhpDocCommentImpl;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import net.miginfocom.swing.MigLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Collection;


public class PhpUnitTestsInspector extends BasePhpInspection {
    // configuration flags automatically saved by IDE
    public boolean SUGGEST_TO_USE_ASSERTSAME = false;

    @NotNull
    public String getShortName() {
        return "PhpUnitTestsInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                PhpClass clazz = method.getContainingClass();
                if (null == clazz) {
                    return;
                }

                String strMethodName     = method.getName();
                PsiElement objMethodName = method.getNameIdentifier();
                if ( null == objMethodName || StringUtil.isEmpty(strMethodName)) {
                    return;
                }

                PhpPsiElement previous = method.getPrevPsiSibling();
                if (!(previous instanceof PhpDocCommentImpl)) {
                    return;
                }

                Collection<PhpDocTag> tags = PsiTreeUtil.findChildrenOfType(previous, PhpDocTag.class);
                for (PhpDocTag tag : tags) {
                    String tagName = tag.getName();

                    if (tagName.equals("@depends") && tag.getFirstPsiChild() instanceof PhpDocRef) {
                        PhpDocRef methodNeeded = (PhpDocRef) tag.getFirstPsiChild();
                        if (!(methodNeeded.resolve() instanceof Method)) {
                            holder.registerProblem(objMethodName, "@depends referencing a non-existing method", ProblemHighlightType.GENERIC_ERROR);
                            continue;
                        }
                    }

                    if (tagName.equals("@covers") && tag.getFirstPsiChild() instanceof PhpDocRef) {
                        PhpDocRef referenceNeeded = (PhpDocRef) tag.getFirstPsiChild();
                        String referenceText      = referenceNeeded.getText();

                        PsiElement resolvedReference = referenceNeeded.resolve();
                        if (
                            null == resolvedReference ||
                            (resolvedReference instanceof PhpClass && referenceText.contains("::") && !referenceText.endsWith("::"))
                        ) {
                            holder.registerProblem(objMethodName, "@covers referencing a non-existing class/method/function", ProblemHighlightType.GENERIC_ERROR);
                        }
                    }
                }
                tags.clear();
            }

            public void visitPhpMethodReference(MethodReference reference) {
                final String methodName = reference.getName();
                if (StringUtil.isEmpty(methodName) || !methodName.startsWith("assert") || methodName.equals("assert")) {
                    return;
                }

                /* strategies injection; TODO: cases with custom messages needs to be handled in each one */

                /* normalize first, no performance tweaks */
                if (
                    AssertBoolInvertedStrategy.apply(methodName, reference, holder)     ||
                    AssertBoolOfComparisonStrategy.apply(methodName, reference, holder)
                ) {
                    return;
                }
                if (SUGGEST_TO_USE_ASSERTSAME) {
                    @SuppressWarnings("unused")
                    boolean strictAsserts =
                        AssertSameStrategy.apply(methodName, reference, holder) ||
                        AssertNotSameStrategy.apply(methodName, reference, holder)
                    ;
                }

                /* now enhance API usage where possible, tweak performance */
                if (
                    AssertCountStrategy.apply(methodName, reference, holder)         ||
                    AssertNotCountStrategy.apply(methodName, reference, holder)      ||

                    AssertNullStrategy.apply(methodName, reference, holder)          ||
                    AssertNotNullStrategy.apply(methodName, reference, holder)       ||

                    AssertEmptyStrategy.apply(methodName, reference, holder)         ||
                    AssertNotEmptyStrategy.apply(methodName, reference, holder)      ||

                    AssertInstanceOfStrategy.apply(methodName, reference, holder)    ||
                    AssertNotInstanceOfStrategy.apply(methodName, reference, holder) ||

                    AssertFileExistsStrategy.apply(methodName, reference, holder)    ||
                    AssertFileNotExistsStrategy.apply(methodName, reference, holder)
                ) {
                    //noinspection UnnecessaryReturnStatement - compact performace tweak
                    return;
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return (new PhpUnitTestsInspector.OptionsPanel()).getComponent();
    }

    public class OptionsPanel {
        final private JPanel optionsPanel;

        final private JCheckBox suggestToUseAssertSame;

        public OptionsPanel() {
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new MigLayout());

            suggestToUseAssertSame = new JCheckBox("Suggest to use assertSame", SUGGEST_TO_USE_ASSERTSAME);
            suggestToUseAssertSame.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    SUGGEST_TO_USE_ASSERTSAME = suggestToUseAssertSame.isSelected();
                }
            });
            optionsPanel.add(suggestToUseAssertSame, "wrap");
        }

        public JPanel getComponent() {
            return optionsPanel;
        }
    }

}
