package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis.classes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
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

    private static final String messagePatternIdentical = "'%s%' method can be dropped, as it identical to parent's one.";
    private static final String messagePatternProxy     = "'%s%' method should call parent's one instead of duplicating code.";

    @NotNull
    public String getShortName() {
        return "SenselessMethodDuplicationInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                /* process non-test and reportable classes only */
                final PhpClass clazz        = method.getContainingClass();
                final PsiElement methodName = NamedElementUtil.getNameIdentifier(method);
                final GroupStatement body   = ExpressionSemanticUtil.getGroupStatement(method);
                if (null == methodName || null == body || null == clazz) {
                    return;
                }
                /* process only real classes and methods */
                if (method.isDeprecated() || clazz.isTrait() || clazz.isInterface() || method.isAbstract()) {
                    return;
                }

                /* don't take too heavy work */
                final int countExpressions = ExpressionSemanticUtil.countExpressionsInGroup(body);
                if (0 == countExpressions || countExpressions > MAX_METHOD_SIZE) {
                    return;
                }

                /* ensure parent, parent methods are existing and contains the same amount of expressions */
                final PhpClass parent           = OpenapiResolveUtil.resolveSuperClass(clazz);
                final Method parentMethod       = null == parent ? null : OpenapiResolveUtil.resolveMethod(parent, method.getName());
                final GroupStatement parentBody = null == parentMethod ? null : ExpressionSemanticUtil.getGroupStatement(parentMethod);
                if (null == parentBody || countExpressions != ExpressionSemanticUtil.countExpressionsInGroup(parentBody)) {
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
                    if (!OpeanapiEquivalenceUtil.areEqual(ownExpression, parentExpression)) {
                            return;
                    }
                    ownExpression    = ownExpression.getNextPsiSibling();
                    parentExpression = parentExpression.getNextPsiSibling();
                }


                /* methods seems to be identical: resolve used classes to avoid ns/imports magic */
                final Collection<String> collection = getUsedReferences(body);
                if (!collection.containsAll(getUsedReferences(parentBody))) {
                    collection.clear();
                    return;
                }
                collection.clear();

                final boolean canFix = !parentMethod.getAccess().isPrivate();
                if (method.getAccess().equals(parentMethod.getAccess())) {
                    final String message = messagePatternIdentical.replace("%s%", method.getName());
                    holder.registerProblem(methodName, message, ProblemHighlightType.WEAK_WARNING, canFix ? new DropMethodFix() : null);
                } else {
                    final String message = messagePatternProxy.replace("%s%", method.getName());
                    holder.registerProblem(methodName, message, ProblemHighlightType.WEAK_WARNING, canFix ? new ProxyCallFix() : null);
                }
            }

            private Collection<String> getUsedReferences(@NotNull GroupStatement body) {
                final Collection<PhpReference> references = PsiTreeUtil.findChildrenOfAnyType(
                        body, ClassReference.class, ConstantReference.class, FunctionReference.class);

                final Set<String> fqns = new HashSet<>(references.size());
                for (PhpReference reference : references) {
                    if (reference instanceof MethodReference) {
                        continue;
                    }

                    final PsiElement entry = OpenapiResolveUtil.resolveReference(reference);
                    if (entry instanceof PhpNamedElement) {
                        fqns.add(((PhpNamedElement) entry).getFQN());
                    }
                }
                references.clear();

                return fqns;
            }
        };
    }

    private static class ProxyCallFix implements LocalQuickFix {
        private static final String title = "Proxy call to parent";

        @NotNull
        @Override
        public String getName() {
            return title;
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
