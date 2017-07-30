package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocRef;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class PhpUnitTestsInspector extends BasePhpInspection {
    // Inspection options.
    public boolean SUGGEST_TO_USE_ASSERTSAME = false;

    private final static String messageDepends = "@depends referencing to a non-existing entity.";
    private final static String messageCovers  = "@covers referencing to a non-existing entity";
    private final static String messageTest    = "@test is ambiguous because method name starts with 'test'.";

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
                final PsiElement objMethodName = NamedElementUtil.getNameIdentifier(method);
                if (null == objMethodName || StringUtils.isEmpty(strMethodName)) {
                    return;
                }
                final boolean isMethodNamedAsTest = strMethodName.startsWith("test");

                final PhpPsiElement previous = method.getPrevPsiSibling();
                if (!(previous instanceof PhpDocComment)) {
                    return;
                }

                final Collection<PhpDocTag> tags  = PsiTreeUtil.findChildrenOfType(previous, PhpDocTag.class);
                for (PhpDocTag tag : tags) {
                    final String tagName = tag.getName();

                    if (tagName.equals("@depends") && tag.getFirstPsiChild() instanceof PhpDocRef) {
                        final PhpDocRef methodNeeded = (PhpDocRef) tag.getFirstPsiChild();
                        /* if resolved properly, it will have 1 reference */
                        if (1 != methodNeeded.getReferences().length) {
                            holder.registerProblem(objMethodName, messageDepends, ProblemHighlightType.GENERIC_ERROR);
                            continue;
                        }
                    }

                    if (tagName.equals("@covers") && tag.getFirstPsiChild() instanceof PhpDocRef) {
                        final PhpDocRef referenceNeeded     = (PhpDocRef) tag.getFirstPsiChild();
                        final String referenceText          = referenceNeeded.getText();
                        final List<PsiReference> references = Arrays.asList(referenceNeeded.getReferences());
                        Collections.reverse(references);

                        /* resolve references, populate information about provided entries */
                        boolean hasCallableReference = false;
                        boolean hasClassReference    = false;

                        final boolean callableNeeded = referenceText.contains("::");
                        for (final PsiReference ref : references) {
                            final PsiElement resolved = ref.resolve();
                            if (resolved instanceof PhpClass) {
                                hasClassReference    = true;
                                hasCallableReference = referenceText.endsWith("::");
                                break;
                            }
                            if (resolved instanceof Function) {
                                hasCallableReference = true;
                                hasClassReference    = resolved instanceof Method;
                                break;
                            }
                        }

                        if ((callableNeeded && !hasCallableReference) || (!callableNeeded && hasClassReference)) {
                            holder.registerProblem(objMethodName, messageCovers, ProblemHighlightType.GENERIC_ERROR);
                            continue;
                        }
                    }

                    if (isMethodNamedAsTest && tagName.equals("@test")) {
                        holder.registerProblem(tag.getFirstChild(), messageTest, ProblemHighlightType.LIKE_DEPRECATED, new AmbiguousTestAnnotationLocalFix());
                    }
                }
                tags.clear();
            }

            public void visitPhpMethodReference(MethodReference reference) {
                final String methodName = reference.getName();
                if (StringUtils.isEmpty(methodName) || !methodName.startsWith("assert") || methodName.equals("assert")) {
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
                    @SuppressWarnings({"unused", "UnusedAssignment"})
                    boolean strictAsserts =
                        AssertSameStrategy.apply(methodName, reference, holder) ||
                        AssertNotSameStrategy.apply(methodName, reference, holder)
                    ;
                }

                /* now enhance API usage where possible, tweak performance */
                if (
                    (new AssertCountStrategy().apply(methodName, reference, holder)) ||
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
                    AssertFileNotExistsStrategy.apply(methodName, reference, holder) ||

                    (new AssertStringEqualsFileStrategy().apply(methodName, reference, holder))

                    // TODO: assertInternalType, assertNotInternalType
                ) {
                    //noinspection UnnecessaryReturnStatement - compact performace tweak
                    return;
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addCheckbox("Suggest to use assertSame", SUGGEST_TO_USE_ASSERTSAME, (isSelected) -> SUGGEST_TO_USE_ASSERTSAME = isSelected);
        });
    }

    private static class AmbiguousTestAnnotationLocalFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getName() {
            return "Drop ambiguous @test";
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement().getParent();
            if (expression instanceof PhpDocTag) {
                /* drop preceding space */
                if (expression.getPrevSibling() instanceof PsiWhiteSpace) {
                    expression.getPrevSibling().delete();
                }

                /* drop preceding star */
                if (expression.getPrevSibling() instanceof LeafPsiElement) {
                    LeafPsiElement previous = (LeafPsiElement) expression.getPrevSibling();
                    if (previous.getText().equals("*")) {
                        expression.getPrevSibling().delete();
                    }
                }

                expression.delete();
            }
        }
    }
}
