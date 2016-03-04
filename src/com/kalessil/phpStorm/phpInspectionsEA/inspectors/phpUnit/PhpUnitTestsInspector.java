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
import java.util.HashSet;


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
                final PhpClass clazz = method.getContainingClass();
                if (null == clazz) {
                    return;
                }

                final String strMethodName     = method.getName();
                final PsiElement objMethodName = method.getNameIdentifier();
                if ( null == objMethodName || StringUtil.isEmpty(strMethodName)) {
                    return;
                }
                final boolean isMethodNamedAsTest = strMethodName.startsWith("test");

                final PhpPsiElement previous = method.getPrevPsiSibling();
                if (!(previous instanceof PhpDocCommentImpl)) {
                    return;
                }

                final HashSet<String> annotations = new HashSet<String>();
                final Collection<PhpDocTag> tags  = PsiTreeUtil.findChildrenOfType(previous, PhpDocTag.class);
                for (PhpDocTag tag : tags) {
                    final String tagName = tag.getName();
                    annotations.add(tagName);

                    if (tagName.equals("@depends") && tag.getFirstPsiChild() instanceof PhpDocRef) {
                        final PhpDocRef methodNeeded = (PhpDocRef) tag.getFirstPsiChild();
                        if (!(methodNeeded.resolve() instanceof Method)) {
                            holder.registerProblem(objMethodName, "@depends referencing a non-existing method", ProblemHighlightType.GENERIC_ERROR);
                            continue;
                        }
                    }

                    if (tagName.equals("@covers") && tag.getFirstPsiChild() instanceof PhpDocRef) {
                        final PhpDocRef referenceNeeded = (PhpDocRef) tag.getFirstPsiChild();
                        final String referenceText      = referenceNeeded.getText();

                        final PsiElement resolvedReference = referenceNeeded.resolve();
                        if (
                            null == resolvedReference ||
                            (resolvedReference instanceof PhpClass && referenceText.contains("::") && !referenceText.endsWith("::"))
                        ) {
                            holder.registerProblem(objMethodName, "@covers referencing a non-existing class/method/function", ProblemHighlightType.GENERIC_ERROR);
                            continue;
                        }
                    }

                    if (isMethodNamedAsTest && tagName.equals("@test")) {
                        holder.registerProblem(tag.getFirstChild(), "@test is ambiguous because method name starts with 'test'", ProblemHighlightType.LIKE_DEPRECATED);
                    }
                }
                tags.clear();


                /* report non-internal methods which are not executed by PhpUnit - dead code/incorrect tests */
                if (
                    clazz.getName().endsWith("Test") &&
                    !annotations.contains("@internal") && !annotations.contains("@dataProvider") &&
                    !(isMethodNamedAsTest || annotations.contains("@test"))
                ) {
                    holder.registerProblem(objMethodName, "This method is not a Unit Test. Annotate it as @internal to suppress this warning.", ProblemHighlightType.GENERIC_ERROR);
                }
                annotations.clear();
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

                    AssertTrueStrategy.apply(methodName, reference, holder)          ||
                    AssertNotTrueStrategy.apply(methodName, reference, holder)       ||

                    AssertFalseStrategy.apply(methodName, reference, holder)         ||
                    AssertNotFalseStrategy.apply(methodName, reference, holder)      ||

                    AssertEmptyStrategy.apply(methodName, reference, holder)         ||
                    AssertNotEmptyStrategy.apply(methodName, reference, holder)      ||

                    AssertInstanceOfStrategy.apply(methodName, reference, holder)    ||
                    AssertNotInstanceOfStrategy.apply(methodName, reference, holder) ||

                    AssertFileExistsStrategy.apply(methodName, reference, holder)    ||
                    AssertFileNotExistsStrategy.apply(methodName, reference, holder)

                    // TODO: assertInternalType, assertNotInternalType
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
