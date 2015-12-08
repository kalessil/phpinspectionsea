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
                final String methodName   = reference.getName();
                final PsiElement[] params = reference.getParameters();
                if (params.length < 2 || StringUtil.isEmpty(methodName)) {
                    return;
                }

                final boolean isAssertEquals = methodName.equals("assertEquals");
                if (!isAssertEquals && !methodName.equals("assertSame")) {
                    return;
                }

                if (SUGGEST_TO_USE_ASSERTSAME && isAssertEquals) {
                    holder.registerProblem(reference, "This check is type-unsafe, consider using assertSame instead", ProblemHighlightType.WEAK_WARNING);
                }

                /* analyze parameters which makes the call equal to assertCount */
                boolean isFirstCount = false;
                if (params[0] instanceof FunctionReference && !(params[0] instanceof MethodReference)) {
                    String referenceName = ((FunctionReference) params[0]).getName();
                    isFirstCount = !StringUtil.isEmpty(referenceName) && referenceName.equals("count");
                }
                boolean isSecondCount = false;
                if (params[1] instanceof FunctionReference && !(params[1] instanceof MethodReference)) {
                    String referenceName = ((FunctionReference) params[1]).getName();
                    isSecondCount = !StringUtil.isEmpty(referenceName) && referenceName.equals("count");
                }
                /* fire assertCount warning when needed */
                if ((isFirstCount && !isSecondCount) || (!isFirstCount && isSecondCount)) {
                    holder.registerProblem(reference, "assertCount should be used instead", ProblemHighlightType.WEAK_WARNING);
                    return;
                }

                /* analyze parameters which makes the call equal to assertNull */
                boolean isFirstNull = false;
                if (params[0] instanceof ConstantReference) {
                    isFirstNull = PhpLangUtil.isNull((ConstantReference) params[0]);
                }
                boolean isSecondNull = false;
                if (params[1] instanceof ConstantReference) {
                    isSecondNull = PhpLangUtil.isNull((ConstantReference) params[1]);
                }
                /* fire assertNull warning when needed */
                if (isFirstNull || isSecondNull) {
                    holder.registerProblem(reference, "assertNull should be used instead", ProblemHighlightType.WEAK_WARNING);
                    // return;
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return (new PhpUnitTestsInspector.OptionsPanel()).getComponent();
    }

    public class OptionsPanel {
        final private JPanel optionsPanel;

        final private JCheckBox suggestToUseassertSame;

        public OptionsPanel() {
            optionsPanel = new JPanel();
            optionsPanel.setLayout(new MigLayout());

            suggestToUseassertSame = new JCheckBox("Suggest to use assertSame", SUGGEST_TO_USE_ASSERTSAME);
            suggestToUseassertSame.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    SUGGEST_TO_USE_ASSERTSAME = suggestToUseassertSame.isSelected();
                }
            });
            optionsPanel.add(suggestToUseassertSame, "wrap");
        }

        public JPanel getComponent() {
            return optionsPanel;
        }
    }

}
