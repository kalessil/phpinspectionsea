package com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocRef;
import com.jetbrains.php.lang.documentation.phpdoc.psi.tags.PhpDocTag;
import com.jetbrains.php.lang.lexer.PhpTokenTypes;
import com.jetbrains.php.lang.psi.elements.*;
import com.kalessil.phpStorm.phpInspectionsEA.inspectors.phpUnit.strategy.*;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.options.OptionsComponent;
import com.kalessil.phpStorm.phpInspectionsEA.utils.ExpressionSemanticUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.MessagesPresentationUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.OpenapiResolveUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

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
    public PhpUnitVersion PHP_UNIT_VERSION       = PhpUnitVersion.PHPUNIT80;
    public boolean SUGGEST_TO_USE_ASSERTSAME     = false;
    public boolean SUGGEST_TO_USE_NAMED_DATASETS = false;
    public boolean PROMOTE_PHPUNIT_API           = true;
    public boolean PROMOTE_MOCKING_ONCE          = true;
    public boolean PROMOTE_MOCKING_WILL_RETURN   = true;

    private final static String messageNamedProvider = "It would be better for maintainability to to use named datasets in @dataProvider.";
    private final static String messageDataProvider  = "@dataProvider referencing to a non-existing entity.";
    private final static String messageDepends       = "@depends referencing to a non-existing or inappropriate entity.";
    private final static String messageCovers        = "@covers referencing to a non-existing entity '%s'";
    private final static String messageTest          = "@test is ambiguous because method name starts with 'test'.";

    @NotNull
    @Override
    public String getShortName() {
        return "PhpUnitTestsInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "PHPUnit: bugs and best practices";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                final PhpClass clazz       = method.getContainingClass();
                final PsiElement nameNode  = NamedElementUtil.getNameIdentifier(method);
                final PhpDocComment phpDoc = method.getDocComment();
                if (null == clazz || null == nameNode || phpDoc == null) {
                    return;
                }

                final boolean isMethodNamedAsTest = method.getName().startsWith("test");
                for (final PhpDocTag tag : PsiTreeUtil.findChildrenOfType(phpDoc, PhpDocTag.class)) {
                    final String tagName = tag.getName();

                    if (tagName.equals("@dataProvider")) {
                        final PsiElement candidate = tag.getFirstPsiChild();
                        if (candidate instanceof PhpDocRef && this.isAnnotation(tag)) {
                            final List<PsiReference> references = Arrays.asList(candidate.getReferences());
                            if (!references.isEmpty()) {
                                Collections.reverse(references);
                                final PsiElement resolved = OpenapiResolveUtil.resolveReference(references.get(0));
                                if (resolved instanceof Method) {
                                    if (SUGGEST_TO_USE_NAMED_DATASETS && !((Method) resolved).isAbstract()) {
                                        final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(resolved);
                                        final PsiElement last     = body == null ? null : ExpressionSemanticUtil.getLastStatement(body);
                                        if (last instanceof PhpReturn) {
                                            final PsiElement value = ExpressionSemanticUtil.getReturnValue((PhpReturn) last);
                                            if (value instanceof ArrayCreationExpression) {
                                                final PsiElement firstChild = ((ArrayCreationExpression) value).getFirstPsiChild();
                                                boolean isNamedDataset      = firstChild == null;
                                                if (firstChild instanceof ArrayHashElement) {
                                                    final PsiElement key = ((ArrayHashElement) firstChild).getKey();
                                                    isNamedDataset       = key instanceof StringLiteralExpression;
                                                }
                                                if (!isNamedDataset) {
                                                    holder.registerProblem(
                                                            nameNode,
                                                            MessagesPresentationUtil.prefixWithEa(messageNamedProvider)
                                                    );
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    holder.registerProblem(
                                            nameNode,
                                            MessagesPresentationUtil.prefixWithEa(messageDataProvider),
                                            ProblemHighlightType.GENERIC_ERROR
                                    );
                                }
                            } else {
                                holder.registerProblem(
                                        nameNode,
                                        MessagesPresentationUtil.prefixWithEa(messageDataProvider),
                                        ProblemHighlightType.GENERIC_ERROR
                                );
                            }
                        }
                    } else if (tagName.equals("@depends")) {
                        final PsiElement candidate = tag.getFirstPsiChild();
                        if (candidate instanceof PhpDocRef && this.isAnnotation(tag)) {
                            final List<PsiReference> references = Arrays.asList(candidate.getReferences());
                            if (!references.isEmpty()) {
                                Collections.reverse(references);
                                final PsiElement resolved = OpenapiResolveUtil.resolveReference(references.get(0));
                                if (resolved instanceof Method) {
                                    final Method dependency = (Method) resolved;
                                    if (!dependency.getName().startsWith("test")) {
                                        final PhpDocComment docBlock = dependency.getDocComment();
                                        if (docBlock == null || docBlock.getTagElementsByName("@test").length == 0) {
                                            holder.registerProblem(
                                                    nameNode,
                                                    MessagesPresentationUtil.prefixWithEa(messageDepends),
                                                    ProblemHighlightType.GENERIC_ERROR
                                            );
                                        }
                                    }
                                } else {
                                    holder.registerProblem(
                                            nameNode,
                                            MessagesPresentationUtil.prefixWithEa(messageDepends),
                                            ProblemHighlightType.GENERIC_ERROR
                                    );
                                }
                            } else {
                                holder.registerProblem(
                                        nameNode,
                                        MessagesPresentationUtil.prefixWithEa(messageDepends),
                                        ProblemHighlightType.GENERIC_ERROR
                                );
                            }
                        }
                    } else if (tagName.equals("@covers")) {
                        final PsiElement candidate = tag.getFirstPsiChild();
                        if (candidate instanceof PhpDocRef && this.isAnnotation(tag)) {
                            final PhpDocRef referenceNeeded     = (PhpDocRef) candidate;
                            final List<PsiReference> references = Arrays.asList(referenceNeeded.getReferences());
                            Collections.reverse(references);

                            /* resolve references, populate information about provided entries */
                            boolean hasCallableReference = false;
                            boolean hasClassReference    = false;
                            final String referenceText   = referenceNeeded.getText();
                            for (final PsiReference ref : references) {
                                final PsiElement resolved = OpenapiResolveUtil.resolveReference(ref);
                                if (resolved instanceof PhpClass) {
                                    hasClassReference    = true;
                                    hasCallableReference = referenceText.endsWith("::");
                                    break;
                                } else if (resolved instanceof Function) {
                                    hasCallableReference = true;
                                    hasClassReference    = resolved instanceof Method;
                                    break;
                                }
                            }

                            final boolean callableNeeded = referenceText.contains("::") && ! referenceText.contains("::<");
                            if ((callableNeeded && !hasCallableReference) || (!callableNeeded && !hasClassReference)) {
                                holder.registerProblem(
                                        nameNode,
                                        MessagesPresentationUtil.prefixWithEa(String.format(messageCovers, referenceText)),
                                        ProblemHighlightType.GENERIC_ERROR
                                );
                            }
                        }
                    } else if (tagName.equals("@test")) {
                        if (isMethodNamedAsTest && this.isAnnotation(tag)) {
                            holder.registerProblem(
                                    tag.getFirstChild(),
                                    MessagesPresentationUtil.prefixWithEa(messageTest),
                                    ProblemHighlightType.LIKE_DEPRECATED,
                                    new AmbiguousTestAnnotationLocalFix()
                            );
                        }
                    }
                }
            }

            private boolean isAnnotation(@NotNull PhpDocTag tag) {
                PsiElement previous      = tag.getPrevSibling();
                previous                 = previous instanceof PsiWhiteSpace ? previous.getPrevSibling() : previous;
                final IElementType start = previous == null ? null : previous.getNode().getElementType();
                return start == PhpTokenTypes.DOC_COMMENT_START || start == PhpTokenTypes.DOC_LEADING_ASTERISK;
            }

            @Override
            public void visitPhpMethodReference(@NotNull MethodReference reference) {
                final String methodName = reference.getName();
                if (methodName != null) {
                    if (methodName.startsWith("assert") && !methodName.equals("assert")) {
                        final List<BooleanSupplier> callbacks = new ArrayList<>();
                        callbacks.add(() -> AssertBoolInvertedStrategy.apply(methodName, reference, holder));
                        callbacks.add(() -> AssertBoolOfComparisonStrategy.apply(methodName, reference, holder));
                        if (SUGGEST_TO_USE_ASSERTSAME) {
                            callbacks.add(() -> AssertSameStrategy.apply(methodName, reference, holder));
                        }
                        if (PROMOTE_PHPUNIT_API) {
                            final PhpUnitVersion version = PHP_UNIT_VERSION == null ? PhpUnitVersion.PHPUNIT80 : PHP_UNIT_VERSION;
                            callbacks.add(() -> AssertEmptyStrategy.apply(methodName, reference, holder));
                            callbacks.add(() -> AssertConstantStrategy.apply(methodName, reference, holder));
                            callbacks.add(() -> AssertInternalTypeStrategy.apply(methodName, reference, holder, version));
                            callbacks.add(() -> AssertInstanceOfStrategy.apply(methodName, reference, holder));
                            callbacks.add(() -> AssertResourceExistsStrategy.apply(methodName, reference, holder, version));
                            callbacks.add(() -> AssertCountStrategy.apply(methodName, reference, holder));
                            callbacks.add(() -> AssertContainsStrategy.apply(methodName, reference, holder, version));
                            callbacks.add(() -> AssertRegexStrategy.apply(methodName, reference, holder));
                            /* AssertFileEqualsStrategy and AssertStringEqualsFileStrategy order is important */
                            callbacks.add(() -> AssertFileEqualsStrategy.apply(methodName, reference, holder));
                            callbacks.add(() -> AssertStringEqualsFileStrategy.apply(methodName, reference, holder));
                        }
                        for (final BooleanSupplier callback : callbacks) {
                            if (callback.getAsBoolean()) {
                                break;
                            }
                        }
                        callbacks.clear();
                    } else if (methodName.equals("expects")) {
                        if (PROMOTE_MOCKING_ONCE) {
                            ExpectsOnceStrategy.apply(methodName, reference, holder);
                        }
                    } else if (methodName.equals("will")) {
                        if (PROMOTE_MOCKING_WILL_RETURN) {
                            WillReturnStrategy.apply(methodName, reference, holder);
                        }
                    }
                }
            }
        };
    }

    public JComponent createOptionsPanel() {
        return OptionsComponent.create((component) -> {
            component.addDropDown("PHPUnit version", PHP_UNIT_VERSION == null ? PhpUnitVersion.PHPUNIT80 : PHP_UNIT_VERSION, (version) -> PHP_UNIT_VERSION = (PhpUnitVersion) version);
            component.addCheckbox("Promote dedicated asserts", PROMOTE_PHPUNIT_API, (isSelected) -> PROMOTE_PHPUNIT_API = isSelected);
            component.addCheckbox("Promote ->once()", PROMOTE_MOCKING_ONCE, (isSelected) -> PROMOTE_MOCKING_ONCE = isSelected);
            component.addCheckbox("Promote ->willReturn*(...)", PROMOTE_MOCKING_WILL_RETURN, (isSelected) -> PROMOTE_MOCKING_WILL_RETURN = isSelected);
            component.addCheckbox("Suggest to use type safe asserts", SUGGEST_TO_USE_ASSERTSAME, (isSelected) -> SUGGEST_TO_USE_ASSERTSAME = isSelected);
            component.addCheckbox("Suggest to use named datasets", SUGGEST_TO_USE_NAMED_DATASETS, (isSelected) -> SUGGEST_TO_USE_NAMED_DATASETS = isSelected);
        });
    }

    private static final class AmbiguousTestAnnotationLocalFix implements LocalQuickFix {
        private static final String title = "Drop ambiguous @test";

        @NotNull
        @Override
        public String getName() {
            return MessagesPresentationUtil.prefixWithEa(title);
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return getName();
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement().getParent();
            if (expression instanceof PhpDocTag && !project.isDisposed()) {
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
