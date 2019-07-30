package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.inspections.PhpInspection;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.DropMethodFix;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.FeaturedPhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.settings.StrictnessCategory;
import com.kalessil.phpStorm.phpInspectionsEA.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class SenselessMethodDuplicationInspector extends PhpInspection {
    // configuration flags automatically saved by IDE
    public int MAX_METHOD_SIZE = 20;
    /* TODO: configurable via drop-down; clean code: 20 lines/method; PMD: 50; Checkstyle: 100 */

    private static final String messagePatternIdentical = "'%s' method can be dropped, as it identical to '%s'.";
    private static final String messagePatternProxy     = "'%s' method should call parent's one instead of duplicating code.";

    @NotNull
    public String getShortName() {
        return "SenselessMethodDuplicationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new FeaturedPhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                if (this.shouldSkipAnalysis(method, StrictnessCategory.STRICTNESS_CATEGORY_UNUSED)) { return; }

                if (!method.isAbstract() && !method.isDeprecated() && !this.isTestContext(method)) {
                    final PhpClass clazz = method.getContainingClass();
                    if (clazz != null && !clazz.isTrait() && !clazz.isInterface()) {
                        final String methodName = method.getName();
                        /* pattern: parent method duplication */
                        final PhpClass parent = OpenapiResolveUtil.resolveSuperClass(clazz);
                        if (parent != null) {
                            final Method parentMethod = OpenapiResolveUtil.resolveMethod(parent, methodName);
                            final boolean matching    = parentMethod != null && !parentMethod.isAbstract() && !parentMethod.isDeprecated() && this.areMatching(method, parentMethod);
                            if (matching) {
                                this.doReporting(method, parentMethod, true);
                            }
                        }
                        /* pattern: used trait method duplication */
                        for (final PhpClass trait : clazz.getTraits()) {
                            final Method traitMethod = OpenapiResolveUtil.resolveMethod(trait, methodName);
                            final boolean matching   = traitMethod != null && !traitMethod.isAbstract() && !traitMethod.isDeprecated() && this.areMatching(method, traitMethod);
                            if (matching) {
                                this.doReporting(method, traitMethod, false);
                            }
                        }
                    }
                }
            }

            private boolean areMatching(@NotNull Method ownMethod, @NotNull Method overriddenMethod) {
                final GroupStatement ownBody = ExpressionSemanticUtil.getGroupStatement(ownMethod);
                final int countExpressions   = ownBody == null ? 0 : ExpressionSemanticUtil.countExpressionsInGroup(ownBody);
                if (countExpressions > 0 && countExpressions <= MAX_METHOD_SIZE) {
                    final GroupStatement overriddenBody = ExpressionSemanticUtil.getGroupStatement(overriddenMethod);
                    if (overriddenBody != null && ExpressionSemanticUtil.countExpressionsInGroup(overriddenBody) == countExpressions) {
                        /* match body expressions */
                        PhpPsiElement ownExpression        = ownBody.getFirstPsiChild();
                        PhpPsiElement overriddenExpression = overriddenBody.getFirstPsiChild();
                        for (int index = 0; index <= countExpressions; ++index) {
                            /* skip doc-blocks */
                            while (ownExpression instanceof PhpDocComment) {
                                ownExpression = ownExpression.getNextPsiSibling();
                            }
                            while (overriddenExpression instanceof PhpDocComment) {
                                overriddenExpression = overriddenExpression.getNextPsiSibling();
                            }
                            if (ownExpression == null || overriddenExpression == null) {
                                break;
                            }
                            if (!OpenapiEquivalenceUtil.areEqual(ownExpression, overriddenExpression)) {
                                return false;
                            }
                            ownExpression        = ownExpression.getNextPsiSibling();
                            overriddenExpression = overriddenExpression.getNextPsiSibling();
                        }

                        /* match imported symbols */
                        final Collection<String> ownSymbols        = this.getUsedReferences(ownBody);
                        final Collection<String> overriddenSymbols = this.getUsedReferences(overriddenBody);
                        final boolean matched                      = ownSymbols.containsAll(overriddenSymbols);
                        ownSymbols.clear();
                        overriddenSymbols.clear();

                        return matched;
                    }
                }

                return false;
            }

            private void doReporting(@NotNull Method ownMethod, @NotNull Method overriddenMethod, boolean canUseProxy) {
                final PsiElement methodName = NamedElementUtil.getNameIdentifier(ownMethod);
                if (methodName != null) {
                    final boolean canFix = !overriddenMethod.getAccess().isPrivate();
                    if (ownMethod.getAccess().equals(overriddenMethod.getAccess())) {
                        holder.registerProblem(
                                methodName,
                                String.format(messagePatternIdentical, ownMethod.getName(), ownMethod.getFQN().replace(".", "::")),
                                canFix ? new DropMethodFix() : null
                        );
                    } else if (canUseProxy) {
                        holder.registerProblem(
                                methodName,
                                String.format(messagePatternProxy, ownMethod.getName()),
                                canFix ? new ProxyCallFix() : null
                        );
                    }
                }

            }

            private Collection<String> getUsedReferences(@NotNull GroupStatement body) {
                final Set<String> fqns = new HashSet<>();
                for (final PhpReference reference : PsiTreeUtil.findChildrenOfAnyType(body, ClassReference.class, ConstantReference.class, FunctionReference.class)) {
                    if (!(reference instanceof MethodReference)) {
                        final PsiElement entry = OpenapiResolveUtil.resolveReference(reference);
                        if (entry instanceof PhpNamedElement) {
                            fqns.add(((PhpNamedElement) entry).getFQN());
                        }
                    }
                }
                return fqns;
            }
        };
    }

    private static final class ProxyCallFix implements LocalQuickFix {
        private static final String title = "Proxy call to parent";

        @NotNull
        @Override
        public String getName() {
            return title;
        }

        @NotNull
        @Override
        public String getFamilyName() {
            return title;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            final PsiElement expression = descriptor.getPsiElement().getParent();
            if (expression instanceof Method && !project.isDisposed()) {
                final Method method = (Method) expression;

                /* pre-collect resources needed for generation */
                final List<String> parameters = new ArrayList<>();
                for (final Parameter parameter: method.getParameters()) {
                    parameters.add('$' + parameter.getName());
                }
                final Set<String> types = new HashSet<>();
                final PhpType resolved  = OpenapiResolveUtil.resolveType(method, project);
                if (resolved != null){
                    resolved.filterUnknown().getTypes().forEach(t -> types.add(Types.getType(t)));
                }
                types.remove(Types.strVoid);

                /* generate replacement and release resources */
                final String pattern = "function() { %r%parent::%m%(%p%); }"
                        .replace("%r%", types.isEmpty() ? "" : "return ")
                        .replace("%m%", method.getName())
                        .replace("%p%", String.join(", ", parameters));
                types.clear();
                parameters.clear();

                final Function donor             = PhpPsiElementFactory.createPhpPsiFromText(project, Function.class, pattern);
                final GroupStatement body        = ExpressionSemanticUtil.getGroupStatement(method);
                final GroupStatement replacement = ExpressionSemanticUtil.getGroupStatement(donor);
                if (null != body && null != replacement) {
                    body.replace(replacement);
                }
            }
        }
    }
}
