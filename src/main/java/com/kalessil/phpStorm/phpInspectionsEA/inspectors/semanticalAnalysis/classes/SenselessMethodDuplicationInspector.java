package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment;
import com.jetbrains.php.lang.psi.PhpPsiElementFactory;
import com.jetbrains.php.lang.psi.elements.*;
import com.jetbrains.php.lang.psi.resolve.types.PhpType;
import com.kalessil.phpStorm.phpInspectionsEA.fixers.DropMethodFix;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
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

public class SenselessMethodDuplicationInspector extends BasePhpInspection {
    // configuration flags automatically saved by IDE
    public int MAX_METHOD_SIZE = 20;
    /* TODO: configurable via drop-down; clean code: 20 lines/method; PMD: 50; Checkstyle: 100 */

    private static final String messagePatternIdentical = "'%s' method can be dropped, as it identical to parent's one.";
    private static final String messagePatternProxy     = "'%s' method should call parent's one instead of duplicating code.";

    @NotNull
    @Override
    public String getShortName() {
        return "SenselessMethodDuplicationInspection";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Child method is exactly the same";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            @Override
            public void visitPhpMethod(@NotNull Method method) {
                /* process only real classes and methods */
                if (method.isAbstract() || method.isDeprecated() || method.getModifier().isPrivate() || this.isTestContext(method)) {
                    return;
                }
                final PhpClass clazz = method.getContainingClass();
                if (clazz == null || clazz.isTrait() || clazz.isInterface()) {
                    return;
                }

                /* don't take too heavy work */
                final GroupStatement body  = ExpressionSemanticUtil.getGroupStatement(method);
                final int countExpressions = body == null ? 0 : ExpressionSemanticUtil.countExpressionsInGroup(body);
                if (countExpressions == 0 || countExpressions > MAX_METHOD_SIZE) {
                    return;
                }

                /* ensure parent, parent methods are existing and contains the same number of expressions */
                final PhpClass parent           = OpenapiResolveUtil.resolveSuperClass(clazz);
                final Method parentMethod       = null == parent ? null : OpenapiResolveUtil.resolveMethod(parent, method.getName());
                if (parentMethod == null || parentMethod.isAbstract() || parentMethod.isDeprecated() || parentMethod.getModifier().isPrivate()) {
                    return;
                }
                final GroupStatement parentBody = ExpressionSemanticUtil.getGroupStatement(parentMethod);
                if (parentBody == null || ExpressionSemanticUtil.countExpressionsInGroup(parentBody) != countExpressions) {
                    return;
                }

                /* iterate and compare expressions */
                PhpPsiElement ownExpression    = body.getFirstPsiChild();
                PhpPsiElement parentExpression = parentBody.getFirstPsiChild();
                for (int index = 0; index <= countExpressions; ++index) {
                    /* skip doc-blocks */
                    while (ownExpression instanceof PhpDocComment) {
                        ownExpression = ownExpression.getNextPsiSibling();
                    }
                    while (parentExpression instanceof PhpDocComment) {
                        parentExpression = parentExpression.getNextPsiSibling();
                    }
                    if (null == ownExpression || null == parentExpression) {
                        break;
                    }

                    /* process comparing 2 nodes */
                    if (!OpenapiEquivalenceUtil.areEqual(ownExpression, parentExpression)) {
                            return;
                    }
                    ownExpression    = ownExpression.getNextPsiSibling();
                    parentExpression = parentExpression.getNextPsiSibling();
                }


                /* methods seem to be identical: resolve used classes to avoid ns/imports magic */
                boolean areReferencesMatching          = true;
                final Collection<String> ownReferences = this.getUsedReferences(body);
                if (! ownReferences.isEmpty()) {
                    final Collection<String> parentReferences = this.getUsedReferences(parentBody);
                    areReferencesMatching                     = ! ownReferences.contains(null) && ownReferences.equals(parentReferences);
                    parentReferences.clear();
                }
                ownReferences.clear();
                if (! areReferencesMatching) {
                    return;
                }

                final PsiElement methodName = NamedElementUtil.getNameIdentifier(method);
                if (methodName != null && !this.isOperatingOnPrivateMembers(parentMethod)) {
                    final boolean canFix = !parentMethod.getAccess().isPrivate();
                    if (method.getAccess().equals(parentMethod.getAccess())) {
                        holder.registerProblem(
                                methodName,
                                String.format(MessagesPresentationUtil.prefixWithEa(messagePatternIdentical), method.getName()),
                                canFix ? new DropMethodFix() : null
                        );
                    } else {
                        holder.registerProblem(
                                methodName,
                                String.format(MessagesPresentationUtil.prefixWithEa(messagePatternProxy), method.getName()),
                                canFix ? new ProxyCallFix() : null
                        );
                    }
                }
            }

            private Collection<String> getUsedReferences(@NotNull GroupStatement body) {
                final Set<String> fqns = new HashSet<>();
                for (final PhpReference reference : PsiTreeUtil.findChildrenOfAnyType(body, ClassReference.class, ConstantReference.class, FunctionReference.class)) {
                    if (! (reference instanceof MethodReference)) {
                        final PsiElement entry = OpenapiResolveUtil.resolveReference(reference);
                        if (entry instanceof PhpNamedElement) {
                            // We have to use this over resolved entry FQN as some of PhpStorm versions might not resolve the proper symbol
                            fqns.add(reference.getFQN());
                        } else {
                            fqns.add(null);
                        }
                    }
                }
                return fqns;
            }

            private boolean isOperatingOnPrivateMembers(@NotNull Method method) {
                final GroupStatement body = ExpressionSemanticUtil.getGroupStatement(method);
                if (body != null) {
                    for (final MemberReference reference : PsiTreeUtil.findChildrenOfType(body, MemberReference.class)) {
                        final PsiElement base = reference.getFirstChild();
                        final boolean resolve = (base instanceof Variable && ((Variable) base).getName().equals("this")) ||
                                                (base instanceof ClassReference && base.getText().equals("self"));
                        if (resolve) {
                            final PsiElement resolved = OpenapiResolveUtil.resolveReference(reference);
                            if (resolved instanceof PhpClassMember && ((PhpClassMember) resolved).getModifier().isPrivate()) {
                                return true;
                            }
                        }
                    }
                }

                return false;
            }
        };
    }

    private static final class ProxyCallFix implements LocalQuickFix {
        private static final String title = "Proxy call to parent";

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
